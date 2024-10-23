package com.qworks.workflow.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.WorkflowActionConfigurationDto;
import com.qworks.workflow.exception.SystemErrorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.qworks.workflow.constants.WorkflowConstants.ID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    public static JsonNode extractNodeFromPath(JsonNode actualData, String path) {
        String[] pathArr = path.split("\\.");
        JsonNode resultNode = actualData;
        for (String key : pathArr) {
            resultNode = resultNode.get(key);
        }

        return resultNode;
    }

    public static String extractTextFromPath(JsonNode actualData, String path) {
        String[] pathArr = path.split("\\.");
        JsonNode resultNode = actualData;
        for (String key : pathArr) {
            resultNode = resultNode.get(key);
        }

        if (Objects.isNull(resultNode) || resultNode.isMissingNode()) {
            return path;
        }

        return resultNode.asText();
    }

    public static String generateBodyJsonForUpdateAction(String id, WorkflowActionConfigurationDto actionConfigurationDto) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(ID, id);
        actionConfigurationDto.getFields().forEach(field -> {
            requestBody.put(field.getKey(), field.getNewValue());
        });

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new SystemErrorException(e.getMessage());
        }
    }

    public static String generateBodyJson(JsonNode triggerDataObj, WorkflowActionConfigurationDto actionConfigurationDto) {
        Map<String, String> requestBody = new HashMap<>();
        actionConfigurationDto.getFields().forEach(field -> {
            String extractValue = extractTextFromPath(triggerDataObj, field.getNewValue());
            requestBody.put(field.getKey(), extractValue);
        });

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new SystemErrorException(e.getMessage());
        }
    }
}
