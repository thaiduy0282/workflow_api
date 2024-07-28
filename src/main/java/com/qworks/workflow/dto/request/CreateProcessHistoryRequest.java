package com.qworks.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProcessHistoryRequest(
        @NotBlank
        String processDefinitionId,

        @NotBlank
        String activityName,

        @NotBlank
        String activityId
) { }

