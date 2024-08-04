package com.qworks.workflow.subscription;

import com.qworks.workflow.service.WorkflowNodeConfigurationService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("pre_complete")
@RequiredArgsConstructor
public class PreCompleteExternalTask implements ExternalTaskHandler {

    private final static Logger LOGGER = Logger.getLogger(PreCompleteExternalTask.class.getName());

    private final WorkflowNodeConfigurationService workflowNodeConfigurationService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        LOGGER.info("==========================================================");
        LOGGER.info(workflowId + " - Completed successfully");

        // Complete the task
        externalTaskService.complete(externalTask);
    }

}
