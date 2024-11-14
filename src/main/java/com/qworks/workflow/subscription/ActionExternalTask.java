package com.qworks.workflow.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.WorkflowActionConfigurationDto;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpException;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.qworks.workflow.constants.WorkflowConstants.*;
import static com.qworks.workflow.util.JsonUtil.generateBodyJson;
import static com.qworks.workflow.util.JsonUtil.generateBodyJsonForUpdateAction;
import static com.qworks.workflow.util.RestTemplateUtil.getHttpHeaders;
import static com.qworks.workflow.util.RestTemplateUtil.getRequestFactory;


@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("action_task")
@RequiredArgsConstructor
@Slf4j
public class ActionExternalTask implements ExternalTaskHandler {

    @Value("${qworks.baseUrl}")
    private String baseUrl;

    private final WorkflowNodeService workflowNodeService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String nodeId = externalTask.getActivityId();
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        log.info("==========================================================");
        log.info(workflowId + " - Start handling the action");

        WorkflowNodeDto configurationDto = this.workflowNodeService.findByWorkflowIdAndNodeId(workflowId, nodeId);

        Boolean isSuccess;
        try {
            isSuccess = handleAction(externalTask, configurationDto);
        } catch (JsonProcessingException | HttpException e) {
            log.info("Occurred an error while handle the action: " + e.getMessage());
            externalTaskService.handleBpmnError(externalTask, "errorInAction", "Occurred an error while handle the action");
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isError", !isSuccess);
        if (Boolean.FALSE.equals(isSuccess)) {
            result.put("errorInTask", nodeId);
        }

        log.info(workflowId + " - End handling the action");
        log.info("==========================================================");

        String redisKey = PROCESS_VARIABLE_REDIS_KEY_PREFIX + externalTask.getProcessInstanceId();
        redisTemplate.opsForHash().putAll(redisKey, result);

        externalTaskService.complete(externalTask);
    }

    private Boolean handleAction(ExternalTask externalTask, WorkflowNodeDto configurationDto) throws JsonProcessingException, HttpException {
        WorkflowActionConfigurationDto actionConfigurationDto = configurationDto.getAction();

        if (actionConfigurationDto.getActionType().equals(ACTION_UPDATE)) {
            String redisKey = PROCESS_VARIABLE_REDIS_KEY_PREFIX + externalTask.getProcessInstanceId();
            String variableName = StringUtils.lowerCase(actionConfigurationDto.getObject());
            String triggerData = (String) redisTemplate.opsForHash().get(redisKey, variableName);

            if (triggerData == null) {
                throw new IllegalStateException("Variable " + variableName + " not found in Redis for key: " + redisKey);
            }

            JsonNode triggerDataObj = objectMapper.readTree(triggerData);
            if (triggerDataObj.isArray() && !triggerDataObj.isEmpty()) {
                for (JsonNode childNode : triggerDataObj) {
                    callUpdateAPI(childNode, actionConfigurationDto);
                }
            } else if (!triggerDataObj.isArray()) {
                callUpdateAPI(triggerDataObj, actionConfigurationDto);
            }
        } else if (actionConfigurationDto.getActionType().equals(ACTION_GET)) {

            String redisKey = PROCESS_VARIABLE_REDIS_KEY_PREFIX + externalTask.getProcessInstanceId();
            String variableName = StringUtils.lowerCase(configurationDto.getTriggerConfiguration().getEventTopic());
            String triggerData = (String) redisTemplate.opsForHash().get(redisKey, variableName);

            if (triggerData == null) {
                throw new IllegalStateException("Variable " + variableName + " not found in Redis for key: " + redisKey);
            }

            JsonNode triggerDataObj = objectMapper.readTree(triggerData);
            callGetAPI(externalTask, triggerDataObj, actionConfigurationDto);
        }

        return true;
    }

    private void callUpdateAPI(JsonNode triggerDataObj, WorkflowActionConfigurationDto actionConfigurationDto) throws HttpException {
        String id = String.valueOf(triggerDataObj.get(ID).asText());
        String jsonBody = generateBodyJsonForUpdateAction(id, actionConfigurationDto);
        String url = this.baseUrl + "metabench/standard?apiName=" + actionConfigurationDto.getObject();
        log.info("Executing update action with API" + url + " for the body: " + jsonBody);
        RestTemplate restTemplate = new RestTemplate(getRequestFactory());
        try {
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, getHttpHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpException();
            }
        } catch(HttpStatusCodeException e) {
            throw e;
        }
    }

    private void callGetAPI(ExternalTask externalTask, JsonNode triggerDataObj,
                            WorkflowActionConfigurationDto actionConfigurationDto) throws HttpException {
        try {
            RestTemplate restTemplate = new RestTemplate(getRequestFactory());
            String jsonBody = generateBodyJson(triggerDataObj, actionConfigurationDto);
            String url = this.baseUrl + "metabench/fetchRecords/" + actionConfigurationDto.getObject() ;
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, getHttpHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpException();
            }

            JsonNode dataArrNode = Objects.requireNonNull(response.getBody()).path(DATA);
            if (dataArrNode.isArray() && !dataArrNode.isArray()) {
                log.info("Found " + dataArrNode.size() + " items in this get request");

                String redisKey = PROCESS_VARIABLE_REDIS_KEY_PREFIX + externalTask.getProcessInstanceId();
                String variableName = StringUtils.lowerCase(actionConfigurationDto.getObject());
                redisTemplate.opsForHash().put(redisKey, variableName, dataArrNode.toString());
            } else {
                log.info("No data was returned from the QWorks system to initiate the workflow");
            }
        } catch(HttpStatusCodeException e) {
            throw e;
        }
    }
}
