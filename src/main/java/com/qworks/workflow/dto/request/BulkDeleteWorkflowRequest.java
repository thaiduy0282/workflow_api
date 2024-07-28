package com.qworks.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkDeleteWorkflowRequest(
        @NotEmpty
        List<@NotBlank String> ids
) {}
