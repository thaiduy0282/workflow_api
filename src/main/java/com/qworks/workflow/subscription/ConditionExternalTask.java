package com.qworks.workflow.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.service.WorkflowNodeService;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
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
public class ConditionExternalTask extends BaseExternalTaskHandler {

    private final static Logger logger = Logger.getLogger(ConditionExternalTask.class.getName());

    public ConditionExternalTask(WorkflowNodeService workflowNodeService) {
        super(workflowNodeService);
    }

    @Override
    public void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String workflowId = getWorkflowId(externalTask);
        logger.info("==========================================================");
        logger.info(workflowId + " - Start checking the conditions");
        WorkflowNodeDto config = workflowNodeService.findByWorkflowIdAndNodeId(workflowId, externalTask.getActivityId());
        String data = externalTask.getVariable(StringUtils.lowerCase(config.getTriggerConfiguration().getEventTopic()));
        boolean conditionResult;
        try {
            conditionResult = evaluateConditions(config, data);
        } catch (Exception e) {
            logger.info("Failed to validate the expression with details message: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("isFailed", true);
            result.put("errMessage", "Failed to validate the expression with details message: " + e.getMessage());
            result.put("isTrue", false);
            externalTaskService.complete(externalTask, result);
            return;
        }

        if (conditionResult) {
            logger.info("Condition passed. Proceeding with the 'Yes' case.");
        } else {
            logger.info("Condition not passed. Proceeding with the 'No' case.");
        }

        // Complete the task
        logger.info(workflowId + " - End checking the conditions");
        logger.info("==========================================================");
        externalTaskService.complete(externalTask, Collections.singletonMap("isTrue", conditionResult));
    }

    private boolean evaluateConditions(WorkflowNodeDto config, String jsonData) throws JsonProcessingException {
        JsonNode dataNode = objectMapper.readTree(jsonData);
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (String ref : config.getCondition().getReferenceObjects()) {
            JsonNode extracted = extractNodeFromPath(dataNode, ref);
            context.setVariable(ref, extracted.asText());
        }

        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(config.getCondition().getExpression());
        return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
    }
}