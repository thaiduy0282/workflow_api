package com.qworks.workflow.subscription;

import com.qworks.workflow.service.WorkflowNodeService;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("pre_complete")
public class PreCompleteExternalTask extends BaseExternalTaskHandler {

    private final static Logger logger = Logger.getLogger(PreCompleteExternalTask.class.getName());

    public PreCompleteExternalTask(WorkflowNodeService workflowNodeService) {
        super(workflowNodeService);
    }

    @Override
    public void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        logger.info("==========================================================");
        logger.info(workflowId + " - Completed successfully");

        // Complete the task
        externalTaskService.complete(externalTask);
    }

}
