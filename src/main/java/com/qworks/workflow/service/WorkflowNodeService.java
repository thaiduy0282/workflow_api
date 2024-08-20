package com.qworks.workflow.service;

import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.dto.request.CreateWorkflowNodeRequest;
import com.qworks.workflow.entity.WorkflowNodeEntity;

import java.util.List;

public interface WorkflowNodeService {

    List<WorkflowNodeDto> findAll();

    WorkflowNodeDto findById(String id);

    List<WorkflowNodeDto> findByWorkflowId(String workflowId);

    WorkflowNodeDto findByWorkflowIdAndNodeId(String workflowId, String nodeId);

    WorkflowNodeDto findByNodeId(String nodeId);

    List<WorkflowNodeDto> create(String workflowId, CreateWorkflowNodeRequest request);

    List<WorkflowNodeEntity> findConfigurationsForUpdateAccount();

}
