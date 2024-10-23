package com.qworks.workflow.controller;

import com.qworks.workflow.dto.WorkflowDto;
import com.qworks.workflow.dto.request.BulkDeleteWorkflowRequest;
import com.qworks.workflow.dto.request.CreateWorkflowRequest;
import com.qworks.workflow.dto.request.ManualTriggerRequest;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping()
    public ResponseEntity<ApiResponse<WorkflowDto>> findAllWorkFlow(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdTime"));
        Pageable pageable = PageRequest.of(page, size, sort);
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
                .message("The workflow was deleted successfully..!")
                .data(id)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<SuccessResponse> bulkDeleteWorkflow(@RequestBody BulkDeleteWorkflowRequest request) {
        workflowService.batchDelete(request.ids());
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Given workflows were deleted successfully..!")
                .data(request.ids())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<SuccessResponse> deleteAllWorkflows() {
        workflowService.deleteAllWorkflows();
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("All workflows were deleted successfully..!")
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

    @PostMapping("/manual-trigger")
    public ResponseEntity<SuccessResponse> manualTrigger(@RequestBody ManualTriggerRequest request) {
        List<String> processesIds = workflowService.manualTrigger(request);
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("There are " + processesIds.size() + " processes that were triggered successfully..!")
                .data(processesIds)
                .build();
        return ResponseEntity.ok(response);
    }
}
