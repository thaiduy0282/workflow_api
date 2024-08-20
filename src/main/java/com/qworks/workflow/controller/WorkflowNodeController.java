package com.qworks.workflow.controller;

import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.dto.request.CreateWorkflowNodeRequest;
import com.qworks.workflow.dto.response.ApiResponse;
import com.qworks.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/node-configuration")
@RequiredArgsConstructor
public class WorkflowNodeController {

    private final WorkflowNodeService workflowNodeService;

    @GetMapping()
    public ResponseEntity<ApiResponse<WorkflowNodeDto>> findAllWorkFlow() {
        List<WorkflowNodeDto> workflows = workflowNodeService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(workflows.size(), workflows));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<List<WorkflowNodeDto>> getNodeConfigurationByWorkflowId(@PathVariable String workflowId) {
        List<WorkflowNodeDto> nodeConfigurationDtos = workflowNodeService.findByWorkflowId(workflowId);
        return ResponseEntity.ok(nodeConfigurationDtos);
    }

    @PostMapping("/{workflowId}")
    public ResponseEntity<List<WorkflowNodeDto>> createWorkflowNodeConfiguration(@PathVariable String workflowId, @RequestBody CreateWorkflowNodeRequest request) {
        List<WorkflowNodeDto> nodeConfigurationDtos = workflowNodeService.create(workflowId, request);
        return ResponseEntity.ok(nodeConfigurationDtos);
    }
}
