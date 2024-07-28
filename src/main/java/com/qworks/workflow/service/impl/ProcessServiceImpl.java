package com.qworks.workflow.service.impl;

import com.qworks.workflow.dto.ProcessDto;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.entity.ProcessEntity;
import com.qworks.workflow.enums.ProcessStatus;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.mapper.ProcessMapper;
import com.qworks.workflow.repository.ProcessRepository;
import com.qworks.workflow.repository.WorkflowRepository;
import com.qworks.workflow.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;

    private final WorkflowRepository workflowRepository;

    private final ProcessDefinitionApi processDefinitionApi;

    private final ProcessMapper processMapper;

    @Override
    public void triggerProcess(String processName, Map<String, VariableValueDto> variables, TriggerProcessRequest triggerProcessRequest) throws ApiException {

        var workflowEntity = workflowRepository.findByProcessDefinitionId(processName)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with processDefinitionId: " + processName));

        StartProcessInstanceDto startProcessInstanceDto = new StartProcessInstanceDto();
        startProcessInstanceDto.setVariables(variables);

        ProcessInstanceWithVariablesDto processInstanceWithVariablesDto = processDefinitionApi.startProcessInstanceByKey(
                processName,
                startProcessInstanceDto
        );

        var processEntity = new ProcessEntity();
        processEntity.setWorkflowId(workflowEntity.getId());
        processEntity.setProcessDefinitionId(workflowEntity.getProcessDefinitionId());
        processEntity.setTriggerTime(new Date());
        processEntity.setTriggerBy(UUID.fromString(triggerProcessRequest.triggerBy()));
        processEntity.setStatus(ProcessStatus.IN_PROGRESS);
        processRepository.save(processEntity);
    }

    @Override
    public Optional<ProcessDto> getProcessById(String id) {
        return processRepository.findById(id)
                .map(processMapper::toDto);
    }

    @Override
    public List<ProcessDto> getAllProcesses(Pageable pageable) {
        return processRepository.findAll(pageable)
                .stream()
                .map(processMapper::toDto)
                .toList();
    }

    @Override
    public void deleteProcess(String id) {
        processRepository.deleteById(id);
    }

}
