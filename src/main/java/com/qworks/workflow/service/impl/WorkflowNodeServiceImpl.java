package com.qworks.workflow.service.impl;

import com.qworks.workflow.constants.WorkflowConstants;
import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.dto.request.CreateWorkflowNodeRequest;
import com.qworks.workflow.entity.WorkflowNodeEntity;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.mapper.WorkflowNodeMapper;
import com.qworks.workflow.repository.WorkflowNodeRepository;
import com.qworks.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowNodeServiceImpl implements WorkflowNodeService {

    private final WorkflowNodeRepository workflowNodeRepository;

    private final WorkflowNodeMapper workflowNodeMapper;

    @Override
    public List<WorkflowNodeDto> findAll() {
        return workflowNodeRepository.findAll().stream()
                .map(workflowNodeMapper::toWorkflowConditionDto)
                .toList();
    }

    @Override
    public WorkflowNodeDto findById(String id) {
        WorkflowNodeEntity workflowNodeEntity = getWorkflowById(id);
        return workflowNodeMapper.toWorkflowConditionDto(workflowNodeEntity);
    }

    @Override
    public List<WorkflowNodeDto> findByWorkflowId(String workflowId) {
        return workflowNodeRepository.findByWorkflowId(workflowId).stream()
                .map(workflowNodeMapper::toWorkflowConditionDto)
                .toList();
    }

    @Override
    public WorkflowNodeDto findByWorkflowIdAndNodeId(String workflowId, String nodeId) {
        return workflowNodeRepository.findByWorkflowIdAndNodeId(workflowId, nodeId).stream()
                .map(workflowNodeMapper::toWorkflowConditionDto)
                .findFirst().get();
    }

    @Override
    public WorkflowNodeDto findByNodeId(String nodeId) {
        return workflowNodeRepository.findByNodeId(nodeId).stream()
                .map(workflowNodeMapper::toWorkflowConditionDto)
                .findFirst().get();
    }

    private WorkflowNodeEntity getWorkflowById(String id) {
        return workflowNodeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(WorkflowConstants.WORKFLOW_NOT_FOUND + id));
    }

    @Override
    public List<WorkflowNodeDto> create(String workflowId, CreateWorkflowNodeRequest request) {
        // check and remove all the existing entries with workflowId in the database before saving the new ones
        workflowNodeRepository.deleteAllByWorkflowId(workflowId);

        List<WorkflowNodeDto> nodeConfigurationDtos = new ArrayList<>();
        request.nodes().forEach(nodeConfiguration -> {
            WorkflowNodeEntity workflowNodeEntity = workflowNodeMapper.toWorkflowConditionEntity(nodeConfiguration);
            workflowNodeEntity.setId(UUID.randomUUID().toString());
            workflowNodeEntity.setWorkflowId(workflowId);
            workflowNodeEntity = workflowNodeRepository.save(workflowNodeEntity);
            WorkflowNodeDto nodeConfigurationDto = workflowNodeMapper.toWorkflowConditionDto(workflowNodeEntity);
            nodeConfigurationDtos.add(nodeConfigurationDto);
        });

        return nodeConfigurationDtos;
    }

    @Override
    public List<WorkflowNodeEntity> findConfigurationsForUpdateAccount() {
        return workflowNodeRepository
                .findByTriggerConfigurationCategoryAndTriggerConfigurationProviderAndTriggerConfigurationEventTopic(
                        "QWORKS", "AKKA", "ACCOUNT_EVENT");
    }
}
