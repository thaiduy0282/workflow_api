package com.qworks.workflow.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public abstract class BaseExternalTaskHandler implements ExternalTaskHandler {
    private final static Logger logger = Logger.getLogger(BaseExternalTaskHandler.class.getName());

    @Value("${qworks.baseUrl}")
    protected String baseUrl;
    protected final WorkflowNodeService workflowNodeService;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public abstract void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService);

    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String workflowId = getWorkflowId(externalTask);
        logger.info("==========================================================");
        logger.info(workflowId + " - Start handling task: " + getClass().getSimpleName());

        try {
            handleTask(externalTask, externalTaskService);
        } catch (Exception e) {
            logger.warning("Error during task execution: " + e.getMessage());
            handleBpmnError(externalTaskService, externalTask, e);
        }

        logger.info(workflowId + " - End handling task: " + getClass().getSimpleName());
        logger.info("==========================================================");
    }

    protected void handleBpmnError(ExternalTaskService service, ExternalTask task, Exception e) {
        service.handleBpmnError(task, "errorOccurred", e.getMessage());
    }

    protected String getWorkflowId(ExternalTask externalTask) {
        return externalTask.getProcessDefinitionKey().split("_")[1];
    }
}
