package com.qworks.workflow.service;

import com.qworks.workflow.dto.WorkflowConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowConfigurationRequest;
import com.qworks.workflow.dto.request.CreateWorkflowNodeRequest;
import com.qworks.workflow.entity.WorkflowConfigurationEntity;

import java.util.List;

public interface WorkflowConfigurationService {

    List<WorkflowConfigurationDto> findAll();

    WorkflowConfigurationDto findById(String id);

    WorkflowConfigurationEntity create(CreateWorkflowConfigurationRequest request);
}
