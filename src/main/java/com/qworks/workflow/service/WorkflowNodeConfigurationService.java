package com.qworks.workflow.service;

import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowNodeConfigurationRequest;
import com.qworks.workflow.entity.WorkflowNodeConfigurationEntity;

import java.util.List;

public interface WorkflowNodeConfigurationService {

    List<WorkflowNodeConfigurationDto> findAll();

    WorkflowNodeConfigurationDto findById(String id);

    List<WorkflowNodeConfigurationDto> findByWorkflowId(String workflowId);

    WorkflowNodeConfigurationDto findByWorkflowIdAndNodeId(String workflowId, String nodeId);

    List<WorkflowNodeConfigurationDto> create(String workflowId, CreateWorkflowNodeConfigurationRequest request);

    List<WorkflowNodeConfigurationEntity> findConfigurationsForUpdateAccount();

}
