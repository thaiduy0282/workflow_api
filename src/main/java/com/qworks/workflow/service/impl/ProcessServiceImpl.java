package com.qworks.workflow.service.impl;

import com.qworks.workflow.dto.ProcessDto;
import com.qworks.workflow.dto.ProcessNodeHistory;
import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.dto.request.UpdateProcessRequest;
import com.qworks.workflow.entity.ProcessEntity;
import com.qworks.workflow.enums.ProcessStatus;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.mapper.ProcessMapper;
import com.qworks.workflow.repository.ProcessRepository;
import com.qworks.workflow.repository.WorkflowRepository;
import com.qworks.workflow.service.ProcessService;
import com.qworks.workflow.service.WorkflowNodeConfigurationService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.qworks.workflow.constants.WorkflowConstants.ADMIN_USER;
import static com.qworks.workflow.constants.WorkflowConstants.END_NODE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;

    private final WorkflowRepository workflowRepository;

    private final ProcessDefinitionApi processDefinitionApi;

    private final ProcessMapper processMapper;

    private final WorkflowNodeConfigurationService workflowNodeConfigurationService;

    @Override
    public void triggerProcess(String processName, Map<String, VariableValueDto> variables, TriggerProcessRequest triggerProcessRequest) throws ApiException {
        var workflowEntity = workflowRepository.findByProcessDefinitionId(processName)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with processDefinitionId: " + processName));

        String processInstanceId = String.valueOf(UUID.randomUUID());
        variables.put("processInstanceId", new VariableValueDto().value(processInstanceId).type("string"));

        StartProcessInstanceDto startProcessInstanceDto = new StartProcessInstanceDto();
        startProcessInstanceDto.setVariables(variables);

        var processEntity = new ProcessEntity();
        processEntity.setWorkflowId(workflowEntity.getId());
        processEntity.setWorkflowName(workflowEntity.getName());
        processEntity.setProcessInstanceId(processInstanceId);
        processEntity.setProcessDefinitionId(workflowEntity.getProcessDefinitionId());
        processEntity.setStartTime(new Date());
        processEntity.setTriggerBy(ADMIN_USER);
        processEntity.setStatus(ProcessStatus.RUNNING);
        processEntity.setDetails(Collections.EMPTY_LIST);
        processRepository.save(processEntity);

        ProcessInstanceWithVariablesDto processInstanceWithVariablesDto = processDefinitionApi.startProcessInstanceByKey(
                processName,
                startProcessInstanceDto
        );
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

    @Override
    public ProcessEntity update(UpdateProcessRequest request) {
        Optional<ProcessEntity> optionalProcessEntity = processRepository.findByProcessInstanceId(request.processInstanceId());
        if (optionalProcessEntity.isEmpty()) {
            throw new ResourceNotFoundException("Process not found with processInstanceId: " + request.processInstanceId());
        }

        ProcessEntity processEntity = optionalProcessEntity.get();
        if (END_NODE.equals(request.activityName())) {
            processEntity.setStatus(request.status());
            processEntity.setEndTime(request.endDate().orElse(new Date()));
        }

        if (ProcessStatus.FAILED.equals(request.status())) {
            processEntity.setStatus(ProcessStatus.FAILED);
            processEntity.setEndTime(request.endDate().orElse(new Date()));
        }

        List<ProcessNodeHistory> processHistory = processEntity.getDetails();
        if (processHistory.isEmpty() || !Objects.equals(processHistory.get(processHistory.size() - 1).getNodeId(), request.nodeId())) {
            ProcessNodeHistory nodeHistory = new ProcessNodeHistory();
            nodeHistory.setStartDate(request.startDate().orElse(new Date()));
            nodeHistory.setEndDate(request.endDate().orElse(null));
            nodeHistory.setStatus(request.status());
            nodeHistory.setNodeId(request.nodeId());

            WorkflowNodeConfigurationDto nodeConfigurationDto = workflowNodeConfigurationService.findByNodeId(request.nodeId());
            nodeHistory.setDisplayName(StringUtils.isEmpty(nodeConfigurationDto.getDisplayName()) ? request.activityName() : nodeConfigurationDto.getDisplayName());
            processEntity.getDetails().add(nodeHistory);
        } else {
            ProcessNodeHistory nodeHistory = processHistory.get(processHistory.size() - 1);
            nodeHistory.setStatus(request.status());
            nodeHistory.setEndDate(request.endDate().orElse(new Date()));
            nodeHistory.setNote(request.note().orElse(""));
        }

        return processRepository.save(processEntity);
    }
}
