package com.qworks.workflow.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.WorkflowActionConfigurationDto;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.dto.WorkflowTriggerConfigurationDto;
import com.qworks.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpException;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

import static com.qworks.workflow.constants.WorkflowConstants.ACTION_GET;
import static com.qworks.workflow.constants.WorkflowConstants.ACTION_UPDATE;
import static com.qworks.workflow.constants.WorkflowConstants.DATA;
import static com.qworks.workflow.constants.WorkflowConstants.ID;
import static com.qworks.workflow.util.JsonUtil.generateBodyJson;
import static com.qworks.workflow.util.JsonUtil.generateBodyJsonForUpdateAction;
import static com.qworks.workflow.util.RestTemplateUtil.getHttpHeaders;
import static com.qworks.workflow.util.RestTemplateUtil.getRequestFactory;


@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("action_task")
@RequiredArgsConstructor
public class ActionExternalTask implements ExternalTaskHandler {

    private final static Logger logger = Logger.getLogger(ActionExternalTask.class.getName());

    @Value("${qworks.baseUrl}")
    private String baseUrl;

    private final WorkflowNodeService workflowNodeService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String nodeId = externalTask.getActivityId();
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        logger.info("==========================================================");
        logger.info(workflowId + " - Start handling the action");

        WorkflowNodeDto configurationDto = this.workflowNodeService.findByWorkflowIdAndNodeId(workflowId, nodeId);

        Boolean isSuccess = null;
        try {
            isSuccess = handleAction(externalTask, externalTaskService, configurationDto);
        } catch (JsonProcessingException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | HttpException e) {
            logger.info("Occurred an error while handle the action: " + e.getMessage());
            externalTaskService.handleBpmnError(externalTask, "errorInAction", "Occurred an error while handle the action");
            return;
        }

        // Complete the task
        Map<String, Object> result = new HashMap<>();
        result.put("isError", !isSuccess);
        if (!isSuccess) {
            result.put("errorInTask", nodeId);
        }

        logger.info(workflowId + " - End handling the action");
        logger.info("==========================================================");
        externalTaskService.complete(externalTask, result);
    }

    private Boolean handleAction(ExternalTask externalTask, ExternalTaskService externalTaskService, WorkflowNodeDto configurationDto)
            throws JsonProcessingException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, HttpException {
        WorkflowActionConfigurationDto actionConfigurationDto = configurationDto.getAction();
        ObjectMapper mapper = new ObjectMapper();

        if (actionConfigurationDto.getActionType().equals(ACTION_UPDATE)) {
            String triggerData = externalTask.getVariable(StringUtils.lowerCase(actionConfigurationDto.getObject()));
            JsonNode triggerDataObj = mapper.readTree(triggerData);
            if (triggerDataObj.isArray() && triggerDataObj.size() > 0) {
                for (JsonNode childNode : triggerDataObj) {
                    callUpdateAPI(childNode, actionConfigurationDto);
                }
            } else if (!triggerDataObj.isArray()) {
                callUpdateAPI(triggerDataObj, actionConfigurationDto);
            }
        } else if (actionConfigurationDto.getActionType().equals(ACTION_GET)) {
            String triggerData = externalTask.getVariable(StringUtils.lowerCase(configurationDto.getTriggerConfiguration().getEventTopic()));
            JsonNode triggerDataObj = mapper.readTree(triggerData);
            callGetAPI(externalTask, externalTaskService, triggerDataObj, actionConfigurationDto);
        }

        return true;
    }

    private void callUpdateAPI(JsonNode triggerDataObj, WorkflowActionConfigurationDto actionConfigurationDto) throws HttpException {
        String id = String.valueOf(triggerDataObj.get(ID).asText());
        String jsonBody = generateBodyJsonForUpdateAction(id, actionConfigurationDto);
        String url = this.baseUrl + "metabench/standard?apiName=" + actionConfigurationDto.getObject();
        logger.info("Executing update action with API" + url + " for the body: " + jsonBody);
        RestTemplate restTemplate = new RestTemplate(getRequestFactory());
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpException();
            }
        } catch(HttpStatusCodeException e) {
            throw e;
        }
    }

    private void callGetAPI(ExternalTask externalTask, ExternalTaskService externalTaskService, JsonNode triggerDataObj,
                            WorkflowActionConfigurationDto actionConfigurationDto) throws HttpException {
        try {
            RestTemplate restTemplate = new RestTemplate(getRequestFactory());
            String jsonBody = generateBodyJson(triggerDataObj, actionConfigurationDto);
            String url = this.baseUrl + "metabench/fetchRecords/" + actionConfigurationDto.getObject() ;
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpException();
            }

            JsonNode dataArrNode = Objects.requireNonNull(response.getBody()).path(DATA);
            if (dataArrNode.isArray() && dataArrNode.size() > 0) {
                logger.info("Found " + dataArrNode.size() + " items in this get request");
                Map<String, Object> variables = externalTask.getAllVariables();
                variables.put(StringUtils.lowerCase(actionConfigurationDto.getObject()), dataArrNode.toString());
                externalTaskService.setVariables(externalTask.getProcessInstanceId(), variables);
            } else {
                logger.info("No data was returned from the QWorks system to initiate the workflow");
            }
        } catch(HttpStatusCodeException e) {
            throw e;
        }
    }
}
