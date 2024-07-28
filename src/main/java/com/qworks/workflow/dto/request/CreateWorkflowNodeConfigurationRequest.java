package com.qworks.workflow.dto.request;

import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;

import java.util.List;

public record CreateWorkflowNodeConfigurationRequest(

        List<WorkflowNodeConfigurationDto> nodeConfigurations
) { }
