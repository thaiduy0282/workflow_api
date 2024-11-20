package com.qworks.workflow.service;

import ai.qworks.dao.nontransaction.workflowapi.ProcessEntity;
import com.qworks.workflow.dto.ProcessDto;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.dto.request.UpdateProcessRequest;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProcessService {

    Page<ProcessDto> findAll(Pageable pageable);

    ProcessInstanceWithVariablesDto triggerProcess(String processName, Map<String, VariableValueDto> variables, TriggerProcessRequest triggerProcessRequest) throws ApiException;

    Optional<ProcessDto> getProcessById(String id);

    List<ProcessDto> getAllProcesses(Pageable pageable);

    void deleteProcess(String id);

    ProcessEntity update(UpdateProcessRequest request);

    void deleteAllProcesses();
}
