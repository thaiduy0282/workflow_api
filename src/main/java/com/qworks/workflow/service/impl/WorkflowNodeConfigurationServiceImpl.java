package com.qworks.workflow.service.impl;

import com.qworks.workflow.constants.WorkflowConstants;
import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowNodeConfigurationRequest;
import com.qworks.workflow.entity.WorkflowNodeConfigurationEntity;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.mapper.WorkflowConditionMapper;
import com.qworks.workflow.repository.WorkflowNodeConfigurationRepository;
import com.qworks.workflow.service.WorkflowNodeConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowNodeConfigurationServiceImpl implements WorkflowNodeConfigurationService {

    private final WorkflowNodeConfigurationRepository workflowNodeConfigurationRepository;

    private final WorkflowConditionMapper workflowConditionMapper;

    @Override
    public List<WorkflowNodeConfigurationDto> findAll() {
        return workflowNodeConfigurationRepository.findAll().stream()
                .map(workflowConditionMapper::toWorkflowConditionDto)
                .toList();
    }

    @Override
    public WorkflowNodeConfigurationDto findById(String id) {
        WorkflowNodeConfigurationEntity workflowNodeConfigurationEntity = getWorkflowById(id);
        return workflowConditionMapper.toWorkflowConditionDto(workflowNodeConfigurationEntity);
    }

    @Override
    public List<WorkflowNodeConfigurationDto> findByWorkflowId(String workflowId) {
        return workflowNodeConfigurationRepository.findByWorkflowId(workflowId).stream()
                .map(workflowConditionMapper::toWorkflowConditionDto)
                .toList();
    }

    @Override
    public WorkflowNodeConfigurationDto findByWorkflowIdAndNodeId(String workflowId, String nodeId) {
        return workflowNodeConfigurationRepository.findByWorkflowIdAndNodeId(workflowId, nodeId).stream()
                .map(workflowConditionMapper::toWorkflowConditionDto)
                .findFirst().get();
    }

    @Override
    public WorkflowNodeConfigurationDto findByNodeId(String nodeId) {
        return workflowNodeConfigurationRepository.findByNodeId(nodeId).stream()
                .map(workflowConditionMapper::toWorkflowConditionDto)
                .findFirst().get();
    }

    private WorkflowNodeConfigurationEntity getWorkflowById(String id) {
        return workflowNodeConfigurationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(WorkflowConstants.WORKFLOW_NOT_FOUND + id));
    }

    @Override
    public List<WorkflowNodeConfigurationDto> create(String workflowId, CreateWorkflowNodeConfigurationRequest request) {
        // check and remove all the existing entries with workflowId in the database before saving the new ones
        workflowNodeConfigurationRepository.deleteAllByWorkflowId(workflowId);

        List<WorkflowNodeConfigurationDto> nodeConfigurationDtos = new ArrayList<>();
        request.nodeConfigurations().forEach(nodeConfiguration -> {
            WorkflowNodeConfigurationEntity workflowNodeConfigurationEntity = workflowConditionMapper.toWorkflowConditionEntity(nodeConfiguration);
            workflowNodeConfigurationEntity.setWorkflowId(workflowId);
            workflowNodeConfigurationEntity = workflowNodeConfigurationRepository.save(workflowNodeConfigurationEntity);
            WorkflowNodeConfigurationDto nodeConfigurationDto = workflowConditionMapper.toWorkflowConditionDto(workflowNodeConfigurationEntity);
            nodeConfigurationDtos.add(nodeConfigurationDto);
        });

        return nodeConfigurationDtos;
    }

    @Override
    public List<WorkflowNodeConfigurationEntity> findConfigurationsForUpdateAccount() {
        return workflowNodeConfigurationRepository
                .findByTriggerConfigurationCategoryAndTriggerConfigurationProviderAndTriggerConfigurationEventTopic(
                        "QWORKS", "AKKA", "ACCOUNT_EVENT");
    }
}
