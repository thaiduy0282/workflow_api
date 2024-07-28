package com.qworks.workflow.controller;

import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;
import com.qworks.workflow.dto.request.CreateWorkflowNodeConfigurationRequest;
import com.qworks.workflow.dto.response.ApiResponse;
import com.qworks.workflow.service.WorkflowNodeConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/node-configuration")
@RequiredArgsConstructor
public class WorkflowNodeConfigurationController {

    private final WorkflowNodeConfigurationService workflowNodeConfigurationService;

    @GetMapping()
    public ResponseEntity<ApiResponse<WorkflowNodeConfigurationDto>> findAllWorkFlow() {
        List<WorkflowNodeConfigurationDto> workflows = workflowNodeConfigurationService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(workflows.size(), workflows));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<List<WorkflowNodeConfigurationDto>> getNodeConfigurationByWorkflowId(@PathVariable String workflowId) {
        List<WorkflowNodeConfigurationDto> nodeConfigurationDtos = workflowNodeConfigurationService.findByWorkflowId(workflowId);
        return ResponseEntity.ok(nodeConfigurationDtos);
    }

    @PostMapping("/{workflowId}")
    public ResponseEntity<List<WorkflowNodeConfigurationDto>> createWorkflowNodeConfiguration(@PathVariable String workflowId, @RequestBody CreateWorkflowNodeConfigurationRequest request) {
        List<WorkflowNodeConfigurationDto> nodeConfigurationDtos = workflowNodeConfigurationService.create(workflowId, request);
        return ResponseEntity.ok(nodeConfigurationDtos);
    }
}
