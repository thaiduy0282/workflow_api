package com.qworks.workflow.service;

import ai.qworks.dao.nontransaction.workflowapi.WorkflowConfigurationEntity;
import com.qworks.workflow.dto.WorkflowConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowConfigurationRequest;

import java.util.List;

public interface WorkflowConfigurationService {

    List<WorkflowConfigurationDto> findAll();

    WorkflowConfigurationDto findById(String id);

    WorkflowConfigurationEntity create(CreateWorkflowConfigurationRequest request);
}
