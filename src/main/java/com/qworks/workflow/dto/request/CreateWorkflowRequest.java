package com.qworks.workflow.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.qworks.workflow.enums.WorkflowType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Optional;
import java.util.UUID;

public record CreateWorkflowRequest(

        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        Optional<String> name,

        Optional<WorkflowType> type,

        JsonNode nodes,

        JsonNode edges,

        @NotNull(message = "Creator ID cannot be null")
        Optional<UUID> createdBy
) { }
