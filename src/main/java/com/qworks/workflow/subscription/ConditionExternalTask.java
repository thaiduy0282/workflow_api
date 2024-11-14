package com.qworks.workflow.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.constants.WorkflowConstants;
import com.qworks.workflow.dto.WorkflowConditionExpressionDto;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.qworks.workflow.util.JsonUtil.extractNodeFromPath;

@Service
@ComponentScan("com.qworks.workflow.service")
@ExternalTaskSubscription("validation_filter")
@RequiredArgsConstructor
@Slf4j
public class ConditionExternalTask implements ExternalTaskHandler {

    private final WorkflowNodeService workflowNodeService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String nodeId = externalTask.getActivityId();
        String workflowId = externalTask.getProcessDefinitionKey().split("_")[1];
        log.info("==========================================================");
        log.info(workflowId + " - Start checking the conditions");

        WorkflowNodeDto configurationDto = this.workflowNodeService.findByWorkflowIdAndNodeId(workflowId, nodeId);
        String triggerData = externalTask.getVariable(StringUtils.lowerCase(configurationDto.getTriggerConfiguration().getEventTopic()));
        ObjectMapper mapper = new ObjectMapper();
        String redisKey = WorkflowConstants.PROCESS_VARIABLE_REDIS_KEY_PREFIX + externalTask.getProcessInstanceId();
        Boolean isTrueCase = true;
        try {
            JsonNode triggerDataObj = mapper.readTree(triggerData);
            isTrueCase = evaluateConditions(configurationDto, triggerDataObj);
        } catch (Exception e) {
            log.info("Failed to validate the expression with details message: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("isFailed", true);
            result.put("errMessage", "Failed to validate the expression with details message: " + e.getMessage());
            result.put("isTrue", false);
            redisTemplate.opsForHash().putAll(redisKey, result);
            externalTaskService.complete(externalTask);
            return;
        }

        if (Boolean.TRUE.equals(isTrueCase)) {
            log.info("Condition passed. Proceeding with the 'Yes' case.");
        } else {
            log.info("Condition not passed. Proceeding with the 'No' case.");
        }

        // Complete the task
        log.info(workflowId + " - End checking the conditions");
        log.info("==========================================================");
        redisTemplate.opsForHash().put(redisKey, "isTrue", isTrueCase);
        externalTaskService.complete(externalTask);
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
