package com.qworks.workflow.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.WorkflowConditionExpressionDto;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.service.WorkflowNodeService;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.qworks.workflow.util.JsonUtil.extractNodeFromPath;

@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("validation_filter")
public class ConditionExternalTask implements ExternalTaskHandler {

    private final static Logger logger = Logger.getLogger(ConditionExternalTask.class.getName());

    @Autowired
    private WorkflowNodeService workflowNodeService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String nodeId = externalTask.getActivityId();
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        logger.info("==========================================================");
        logger.info(workflowId + " - Start checking the conditions");

        WorkflowNodeDto configurationDto = this.workflowNodeService.findByWorkflowIdAndNodeId(workflowId, nodeId);
        String triggerData = externalTask.getVariable(StringUtils.lowerCase(configurationDto.getTriggerConfiguration().getEventTopic()));
        ObjectMapper mapper = new ObjectMapper();

        Boolean isTrueCase = true;
        try {
            JsonNode triggerDataObj = mapper.readTree(triggerData);
            isTrueCase = evaluateConditions(configurationDto, triggerDataObj);
        } catch (Exception e) {
            logger.info("Failed to validate the expression with details message: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("isFailed", true);
            result.put("errMessage", "Failed to validate the expression with details message: " + e.getMessage());
            result.put("isTrue", false);
            externalTaskService.complete(externalTask, result);
            return;
        }

        if (isTrueCase) {
            logger.info("Condition passed. Proceeding with the 'Yes' case.");
        } else {
            logger.info("Condition not passed. Proceeding with the 'No' case.");
        }

        // Complete the task
        logger.info(workflowId + " - End checking the conditions");
        logger.info("==========================================================");
        externalTaskService.complete(externalTask, Collections.singletonMap("isTrue", isTrueCase));
    }

    private Boolean evaluateConditions(WorkflowNodeDto configurationDto, JsonNode actualData) {
        Boolean isPassed = false;
        WorkflowConditionExpressionDto condition = configurationDto.getCondition();
        Map<String, Object> myVariables = new HashMap<>();
        for(String key : condition.getReferenceObjects()) {
            JsonNode extractNode = extractNodeFromPath(actualData, key);
            myVariables.put(key, extractNode.asText());
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(myVariables);

        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(condition.getExpression());
        isPassed = expression.getValue(context, Boolean.class);

        return isPassed;
    }
}
