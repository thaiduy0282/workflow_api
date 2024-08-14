package com.qworks.workflow.controller;

import com.qworks.workflow.dto.WorkflowDto;
import com.qworks.workflow.dto.request.BulkDeleteWorkflowRequest;
import com.qworks.workflow.dto.request.CreateWorkflowRequest;
import com.qworks.workflow.dto.request.TestWorkflowRequest;
import com.qworks.workflow.dto.request.UpdateWorkflowRequest;
import com.qworks.workflow.dto.response.ApiResponse;
import com.qworks.workflow.exception.BPMNException;
import com.qworks.workflow.exception.model.SuccessResponse;
import com.qworks.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v1/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping()
    public ResponseEntity<ApiResponse<WorkflowDto>> findAllWorkFlow(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkflowDto> workflows = workflowService.findAll(pageable);

        ApiResponse<WorkflowDto> response = new ApiResponse<>(
                workflows.getTotalElements(),
                size,
                page,
                workflows.getTotalPages(),
                workflows.getContent()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDto> getWorkflowById(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.findById(id));
    }

    @PostMapping()
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody CreateWorkflowRequest request) {
        WorkflowDto createdWorkflow = workflowService.create(request);
        return ResponseEntity.ok(createdWorkflow);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable String id,
                                                      @RequestBody UpdateWorkflowRequest request) {
        return ResponseEntity.ok(workflowService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteWorkflow(@PathVariable String id) {
        workflowService.delete(id);
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Data has been deleted successfully..!")
                .data(id)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/all")
    public ResponseEntity<SuccessResponse> bulkDeleteWorkflow(@RequestBody BulkDeleteWorkflowRequest request) {
        workflowService.batchDelete(request.ids());
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Data has been deleted successfully..!")
                .data(request.ids())
                .build();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{workflowId}/publish")
    public ResponseEntity<SuccessResponse> publish(@PathVariable String workflowId, @RequestParam boolean isPublished) {
        workflowService.publishWorkflow(workflowId, isPublished);
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workflow is published successfully..!")
                .data(workflowId)
                .build();
        return ResponseEntity.ok(response);
    }
}
