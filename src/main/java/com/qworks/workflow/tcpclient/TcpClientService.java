package com.qworks.workflow.tcpclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.entity.WorkflowNodeConfigurationEntity;
import com.qworks.workflow.service.ProcessService;
import com.qworks.workflow.service.WorkflowNodeConfigurationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final WorkflowNodeConfigurationService workflowNodeConfigurationService;

    private Socket socket;
    private BufferedReader reader;

    @PostConstruct
    public void init() {
        scheduler.schedule(this::connectToTcpServer, INITIAL_RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
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
            TypeReference<AkkaMessagePayload> typeRef = new TypeReference<>() {};
            AkkaMessagePayload akkaMessagePayload = objectMapper.readValue(message, typeRef);
            if (akkaMessagePayload.getRoutingKey().equals("ACCOUNT_EVENT")) {
                String lastModifiedDate = akkaMessagePayload.getPayload().get("lastModifiedDate").asText();
                JsonNode apiResponse = callApiWithTimestamp(Long.parseLong(lastModifiedDate));
                if (apiResponse != null) {
                    List<WorkflowNodeConfigurationEntity> configurations = workflowNodeConfigurationService.findConfigurationsForUpdateAccount();
                    Map<String, Boolean> checkMap = new HashMap<>();
                    configurations.forEach(config -> {
                        if (!checkMap.containsKey(config.getWorkflowId())) {
                            log.info("Trigger the process for the workflow: {}", config.getWorkflowId());
                            checkMap.put(config.getWorkflowId(), true);
                            triggerProcess(config.getWorkflowId(), apiResponse);
                        }
                    });
                }
            } else {
                log.info("Processing message with routing key {} for {}: {}", akkaMessagePayload.getRoutingKey(), akkaMessagePayload.getProperties().getContentType(), akkaMessagePayload.getPayload());
                // triggerAccountProcess(tcpMessage.getPayload());
            }
        } catch (Exception e) {
            log.error("processMessage fail: {}", message, e);
        }
    }

    private JsonNode callApiWithTimestamp(long timestamp) {
        log.info("Fetching the list of users are updated within the timestamp: {}", timestamp);
        String apiUrl = baseUrl + "metabench/fetchRecords/Account";
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
                log.error("Unexpected response code: " + response.getStatusCode());
            }
        } catch(HttpStatusCodeException e) {
            log.error("Error calling API: ", e);
        }

        return null;
    }

    private void triggerProcess(String workflowId, JsonNode apiResponse) {
        try {
            JsonNode dataNode = apiResponse.path("data");
            if (dataNode.isArray()) {
                for (JsonNode accountNode : dataNode) {
                    Map<String, VariableValueDto> variables = new HashMap<>();
                    variables.put("triggerData", new VariableValueDto().value(accountNode.toString()).type("string"));
                    processService.triggerProcess("definition_" + workflowId, variables, new TriggerProcessRequest(""));
                }
            }
        } catch (Exception e) {
            log.error("Error triggering process for workflowId {}: ", workflowId, e);
        }
    }
}
