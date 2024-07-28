package com.qworks.workflow.controller;

import com.qworks.workflow.dto.WorkflowDto;
import com.qworks.workflow.dto.request.BulkDeleteWorkflowRequest;
import com.qworks.workflow.dto.request.CreateWorkflowRequest;
import com.qworks.workflow.dto.request.TestWorkflowRequest;
import com.qworks.workflow.dto.request.UpdateWorkflowRequest;
import com.qworks.workflow.dto.response.ApiResponse;
import com.qworks.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Void> deleteWorkflow(@PathVariable String id) {
        workflowService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> bulkDeleteWorkflow(@RequestBody BulkDeleteWorkflowRequest request) {
        workflowService.batchDelete(request.ids());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{workflowId}/publish")
    public ResponseEntity<Object> publish(@PathVariable String workflowId, @RequestParam boolean isPublished) throws IOException, ApiException {
        workflowService.publishWorkflow(workflowId, isPublished);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/generate")
    public ResponseEntity<Object> generate(@RequestBody TestWorkflowRequest request) throws IOException, ApiException {
        workflowService.generateBPMNProcess(WorkflowDto.builder().id(request.workflowId()).build(), request.nodes(), request.edges());
        return ResponseEntity.noContent().build();    }
}
