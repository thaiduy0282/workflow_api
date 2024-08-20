package com.qworks.workflow.controller;

import com.qworks.workflow.dto.WorkflowConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowConfigurationRequest;
import com.qworks.workflow.dto.response.ApiResponse;
import com.qworks.workflow.entity.WorkflowConfigurationEntity;
import com.qworks.workflow.service.WorkflowConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/workflow-configuration")
@RequiredArgsConstructor
public class WorkflowConfigurationController {

    private final WorkflowConfigurationService workflowConfigurationService;

    @GetMapping()
    public ResponseEntity<ApiResponse<WorkflowConfigurationDto>> findAllWorkFlow() {
        List<WorkflowConfigurationDto> workflows = workflowConfigurationService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(workflows.size(), workflows));
    }

    @PostMapping()
    public ResponseEntity<WorkflowConfigurationEntity> create(@RequestBody CreateWorkflowConfigurationRequest request) {
        WorkflowConfigurationEntity workflowConfigurationEntity = workflowConfigurationService.create(request);
        return ResponseEntity.ok(workflowConfigurationEntity);
    }
}
