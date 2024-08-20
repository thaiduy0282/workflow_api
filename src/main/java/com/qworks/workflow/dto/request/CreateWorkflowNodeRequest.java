package com.qworks.workflow.dto.request;

import com.qworks.workflow.dto.WorkflowNodeDto;

import java.util.List;

public record CreateWorkflowNodeRequest(

        List<WorkflowNodeDto> nodes
) { }
