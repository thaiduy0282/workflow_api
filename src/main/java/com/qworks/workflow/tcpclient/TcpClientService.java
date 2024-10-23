package com.qworks.workflow.tcpclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.service.ProcessService;
import com.qworks.workflow.service.WorkflowNodeService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.qworks.workflow.constants.WorkflowConstants.ALLOWED_TRIGGER_OBJECTS;
import static com.qworks.workflow.constants.WorkflowConstants.DATA;
import static com.qworks.workflow.util.RestTemplateUtil.getHttpHeaders;
import static com.qworks.workflow.util.RestTemplateUtil.getRequestFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcpClientService {

    @Value("${tcp.server.address}")
    private String serverAddress;

    @Value("${tcp.server.port}")
    private int serverPort;

    @Value("${qworks.baseUrl}")
    private String baseUrl;

    private static final int INITIAL_RECONNECT_DELAY_SECONDS = 10;
    private static final int MAX_RECONNECT_DELAY_SECONDS = 600;

    private volatile boolean keepRunning = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProcessService processService;
    private final WorkflowNodeService workflowNodeService;

    private Socket socket;
    private BufferedReader reader;

    @PostConstruct
    public void init() {
//        scheduler.schedule(this::connectToTcpServer, INITIAL_RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private synchronized void connectToTcpServer() {
        int reconnectDelay = INITIAL_RECONNECT_DELAY_SECONDS;
        while (keepRunning) {
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(serverAddress, serverPort);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    log.info("Connected to server at {}:{}", serverAddress, serverPort);

                    new Thread(this::readMessages).start();
                }
                reconnectDelay = INITIAL_RECONNECT_DELAY_SECONDS;
            } catch (Exception e) {
                log.error("Error connecting to server: ", e);
                if (keepRunning) {
                    log.info("Attempting to reconnect {}:{} in {} seconds...",  serverAddress, serverPort, reconnectDelay);
                    try {
                        wait(reconnectDelay * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Reconnect attempt interrupted", ie);
                    }
                    reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY_SECONDS);
                }
            }
        }
    }

    private void readMessages() {
        String message;
        try {
            while ((message = reader.readLine()) != null && keepRunning) {
                log.info("==========================================================");
                log.info("Received AKKA Message: {}", message);
                processMessage(message);
            }
        } catch (Exception e) {
            log.error("Error reading messages: ", e);
        }
    }

    @PreDestroy
    public synchronized void shutdown() {
        keepRunning = false;
        scheduler.shutdownNow();
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            log.error("Error closing socket: ", e);
        }
        log.info("Shutting down server connection.");
    }

    private void processMessage(String message) {
        try {
            TypeReference<EventPayload> typeRef = new TypeReference<>() {};
            EventPayload EventPayload = objectMapper.readValue(message, typeRef);
            if (ALLOWED_TRIGGER_OBJECTS.contains(EventPayload.getRoutingKey())) {
                String lastModifiedDate = EventPayload.getPayload().get("lastModifiedDate").asText();
                JsonNode apiResponse = callApiWithTimestamp(EventPayload.getRoutingKey(), Long.parseLong(lastModifiedDate));
                if (apiResponse != null) {
                    List<String> configurations = workflowNodeService.getUniqueWorkflowIdBaseOnTriggerObject(EventPayload.getRoutingKey());
                    if (configurations.isEmpty()) {
                        log.info("No workflows are configured with the trigger object {} then skipped it", EventPayload.getRoutingKey());
                        return;
                    }

                    log.info("Found {} workflows with trigger object {}: {}", configurations.size(), EventPayload.getRoutingKey(), StringUtils.join(configurations));
                    configurations.forEach(workflowId -> {
                        triggerProcess(workflowId, apiResponse);
                    });
                }
            } else {
                log.info("The workflow app does not support this trigger object {} for {}: {}", EventPayload.getRoutingKey(), EventPayload.getProperties().getContentType(), EventPayload.getPayload());
            }
        } catch (Exception e) {
            log.error("processMessage fail: {}", message, e);
        }
    }

    private JsonNode callApiWithTimestamp(String triggerObject, long timestamp) {
        log.info("Fetching the list of {} objects that were updated within the specified timestamp: {}", triggerObject, timestamp);
        String apiUrl = baseUrl + "metabench/fetchRecords/" + triggerObject;
        RestTemplate restTemplate = new RestTemplate(getRequestFactory());
        try {
            HttpHeaders headers = getHttpHeaders();
            String json = "{ \"lastmodifieddate\": " + timestamp + " }";
            HttpEntity<String> entity = new HttpEntity<String>(json, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("API response: " + response.getBody());
                return response.getBody();
            } else {
                log.error("Unexpected code {} returning while calling the API to fetch {} objects updated within the specified timestamp: {} ", response.getStatusCode(), triggerObject, timestamp);
            }
        } catch(HttpStatusCodeException e) {
            log.error("An error occurred while calling the API to fetch {} objects updated within the specified timestamp: {}", triggerObject, timestamp, e);
        }

        return null;
    }

    private void triggerProcess(String workflowId, JsonNode apiResponse) {
        try {
            JsonNode dataArrNode = apiResponse.path(DATA);
            if (dataArrNode.isArray() && dataArrNode.size() > 0) {
                for (JsonNode childNode : dataArrNode) {
                    Map<String, VariableValueDto> variables = new HashMap<>();
                    variables.put("triggerData", new VariableValueDto().value(childNode.toString()).type("string"));
                    log.info("Executing workflow {} with data: {}", workflowId, childNode.toString());
                    processService.triggerProcess("definition_" + workflowId, variables, new TriggerProcessRequest(""));
                }
            } else {
                log.info("No data was returned from the QWorks system to initiate the workflow {}", workflowId);
            }
        } catch (Exception e) {
            log.error("Error initiating process for workflowId {}: ", workflowId, e);
        }
    }
}
