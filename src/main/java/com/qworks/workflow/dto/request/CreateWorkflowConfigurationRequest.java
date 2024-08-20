package com.qworks.workflow.dto.request;

public record CreateWorkflowConfigurationRequest(
        String id,

        String key,

        String description,

        Object value
) { }
