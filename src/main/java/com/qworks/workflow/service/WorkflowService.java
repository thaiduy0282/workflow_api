package com.qworks.workflow.service;

import com.qworks.workflow.dto.Edge;
import com.qworks.workflow.dto.Node;
import com.qworks.workflow.dto.WorkflowDto;
import com.qworks.workflow.dto.request.CreateWorkflowRequest;
import com.qworks.workflow.dto.request.UpdateWorkflowRequest;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface WorkflowService {

    Page<WorkflowDto> findAll(Pageable pageable);

    WorkflowDto findById(String id);

    WorkflowDto create(CreateWorkflowRequest request);

    WorkflowDto update(String id, UpdateWorkflowRequest request);

    void delete(String id);

    void batchDelete(List<String> ids);

    File generateBPMNProcess(WorkflowDto workflowDto, List<Node> nodes, List<Edge> edges) throws IOException, ApiException;

    void publishWorkflow(String workflowId, boolean isPublished) throws IOException, ApiException;

}
