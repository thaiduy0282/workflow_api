package com.qworks.workflow.service.impl;

import com.qworks.workflow.constants.WorkflowConstants;
import com.qworks.workflow.dto.WorkflowConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowConfigurationRequest;
import com.qworks.workflow.entity.WorkflowConfigurationEntity;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.mapper.WorkflowConfigurationMapper;
import com.qworks.workflow.repository.WorkflowConfigurationRepository;
import com.qworks.workflow.service.WorkflowConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowConfigurationServiceImpl implements WorkflowConfigurationService {

    private final WorkflowConfigurationRepository workflowConfigurationRepository;

    private final WorkflowConfigurationMapper workflowConfigurationMapper;

    @Override
    public List<WorkflowConfigurationDto> findAll() {
        return workflowConfigurationRepository.findAll().stream()
                .map(workflowConfigurationMapper::toWorkflowConfigurationDto)
                .toList();
    }

    @Override
    public WorkflowConfigurationDto findById(String id) {
        WorkflowConfigurationEntity workflowConfigurationEntity = getWorkflowById(id);
        return workflowConfigurationMapper.toWorkflowConfigurationDto(workflowConfigurationEntity);
    }

    private WorkflowConfigurationEntity getWorkflowById(String id) {
        return workflowConfigurationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(WorkflowConstants.WORKFLOW_NOT_FOUND + id));
    }

    @Override
    public WorkflowConfigurationEntity create(CreateWorkflowConfigurationRequest request) {
        WorkflowConfigurationEntity workflowConfigurationEntity = new WorkflowConfigurationEntity();
        workflowConfigurationEntity.setId(request.id());
        workflowConfigurationEntity.setKey(request.key());
        workflowConfigurationEntity.setDescription(request.description());
        workflowConfigurationEntity.setValue(request.value());
        workflowConfigurationEntity.setCreatedDate(new Date());
        workflowConfigurationEntity.setLastModifiedDate(new Date());

        return workflowConfigurationRepository.save(workflowConfigurationEntity);
    }
}
