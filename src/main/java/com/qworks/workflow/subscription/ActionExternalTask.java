package com.qworks.workflow.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.qworks.workflow.dto.WorkflowActionConfigurationDto;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.service.WorkflowNodeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpException;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.qworks.workflow.constants.WorkflowConstants.ACTION_GET;
import static com.qworks.workflow.constants.WorkflowConstants.ACTION_UPDATE;
import static com.qworks.workflow.util.JsonUtil.generateBodyJson;
import static com.qworks.workflow.util.RestTemplateUtil.getHttpHeaders;
import static com.qworks.workflow.util.RestTemplateUtil.getRequestFactory;


@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("action_task")
public class ActionExternalTask extends BaseExternalTaskHandler {

    private final static Logger logger = Logger.getLogger(ActionExternalTask.class.getName());

    public ActionExternalTask(WorkflowNodeService workflowNodeService) {
        super(workflowNodeService);
    }

    @Override
    public void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String workflowId = getWorkflowId(externalTask);
        logger.info("==========================================================");
        logger.info(workflowId + " - Start handling the action");

        WorkflowNodeDto configuration = workflowNodeService.findByWorkflowIdAndNodeId(workflowId, externalTask.getActivityId());
        WorkflowActionConfigurationDto actionConfig = configuration.getAction();

        try {
            if (ACTION_UPDATE.equals(actionConfig.getActionType())) {
                handleUpdateAction(externalTask, actionConfig);
            } else if (ACTION_GET.equals(actionConfig.getActionType())) {
                handleGetAction(externalTask, externalTaskService, actionConfig);
            }
            completeTask(externalTaskService, externalTask, true);
        } catch (Exception e) {
            completeTask(externalTaskService, externalTask, false, "Failed to handle action: " + e.getMessage());
        }
    }

    private void handleUpdateAction(ExternalTask task, WorkflowActionConfigurationDto config) throws JsonProcessingException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, HttpException {
        JsonNode data = objectMapper.readTree(task.getVariable(StringUtils.lowerCase(config.getObject())).toString());
        if (data.isArray()) {
            for (JsonNode item : data) {
                callApi(item, config, HttpMethod.POST);
            }
        } else {
            callApi(data, config, HttpMethod.POST);
        }
    }

    private void handleGetAction(ExternalTask task, ExternalTaskService taskService, WorkflowActionConfigurationDto config) throws JsonProcessingException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, HttpException {
        JsonNode data = objectMapper.readTree(task.getVariable(StringUtils.lowerCase(config.getObject())).toString());
        JsonNode response = callApi(data, config, HttpMethod.GET);
        if (response != null) {
            taskService.setVariables(task.getProcessInstanceId(), Map.of(StringUtils.lowerCase(config.getObject()), response.toString()));
        }
    }

    private JsonNode callApi(JsonNode data, WorkflowActionConfigurationDto config, HttpMethod method) throws HttpException {
        String url = baseUrl + (method == HttpMethod.POST ? "updateEndpoint" : "fetchRecordsEndpoint");
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(generateBodyJson(data, config), headers);

        ResponseEntity<JsonNode> response = new RestTemplate(getRequestFactory()).exchange(url, method, entity, JsonNode.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new HttpException("Non-successful response");
        }
        return response.getBody();
    }

    private void completeTask(ExternalTaskService service, ExternalTask task, boolean isSuccess, String... errorMessages) {
        Map<String, Object> result = new HashMap<>();
        result.put("isError", !isSuccess);
        if (!isSuccess && errorMessages.length > 0) {
            result.put("errorInTask", errorMessages[0]);
        }
        service.complete(task, result);
    }
}