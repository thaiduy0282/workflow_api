package com.qworks.workflow.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;
import com.qworks.workflow.dto.WorkflowTriggerConfigurationDto;
import com.qworks.workflow.enums.EventCategory;
import com.qworks.workflow.exception.SystemErrorException;
import com.qworks.workflow.service.WorkflowNodeConfigurationService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static com.qworks.workflow.util.RestTemplateUtil.getHttpHeaders;
import static com.qworks.workflow.util.RestTemplateUtil.getRequestFactory;


@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("action_task")
@RequiredArgsConstructor
public class ActionExternalTask implements ExternalTaskHandler {

    private final static Logger LOGGER = Logger.getLogger(ActionExternalTask.class.getName());
    private final static String BASE_URL = "https://dev.qworks.ai/metabench/api/";

    private final WorkflowNodeConfigurationService workflowNodeConfigurationService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String nodeId = externalTask.getActivityId();
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        LOGGER.info("==========================================================");
        LOGGER.info(workflowId + " - Start handling the action");

        WorkflowNodeConfigurationDto configurationDto = this.workflowNodeConfigurationService.findByWorkflowIdAndNodeId(workflowId, nodeId);
        String triggerData = (String) externalTask.getVariable("triggerData");

        // Update account info
        Boolean isSuccess = null;
        try {
            isSuccess = handleAction(triggerData, configurationDto);
        } catch (JsonProcessingException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.info("Occurred an error while handle the action: " + e.getMessage());
            externalTaskService.handleBpmnError(externalTask, "errorInAction", "Occurred an error while handle the action");
            return;
        }

        // Complete the task
        Map<String, Object> result = new HashMap<>();
        result.put("isError", !isSuccess);
        if (!isSuccess) {
            result.put("errorInTask", nodeId);
        }

        LOGGER.info(workflowId + " - End handling the action");
        LOGGER.info("==========================================================");
        externalTaskService.complete(externalTask, result);
    }

    private Boolean handleAction(String triggerData, WorkflowNodeConfigurationDto configurationDto) throws JsonProcessingException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        if (configurationDto.getAction().getActionType().equals("Update data")) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode triggerDataObj = mapper.readTree(triggerData);
            String id = String.valueOf(triggerDataObj.get("id").asText());

            String jsonBody = generateBodyJson(id, configurationDto);
            WorkflowTriggerConfigurationDto triggerConfigurationDto = configurationDto.getTriggerConfiguration();
            if (triggerConfigurationDto.getCategory() == EventCategory.QWORKS && Objects.equals(triggerConfigurationDto.getEventTopic(), "ACCOUNT_EVENT")) {
                String baseUrl = BASE_URL + "metabench/standard?apiName=Account";
                RestTemplate restTemplate = new RestTemplate(getRequestFactory());
                try {
                    HttpHeaders headers = getHttpHeaders();
                    HttpEntity<String> entity = new HttpEntity<String>(jsonBody, headers);
                    restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
                } catch(HttpStatusCodeException e) {
                    return false;
                }
            }
        }

        return true;
    }

    private String generateBodyJson(String id, WorkflowNodeConfigurationDto configurationDto) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", id);
        configurationDto.getAction().getFields().forEach(field -> {
            requestBody.put(field.getKey(), field.getNewValue());
            LOGGER.info("Updated value: " + field.getKey() + " - " + field.getNewValue());
        });

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new SystemErrorException(e.getMessage());
        }
    }
}
