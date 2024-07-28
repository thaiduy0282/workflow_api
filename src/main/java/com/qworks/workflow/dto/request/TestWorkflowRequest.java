package com.qworks.workflow.dto.request;

import com.qworks.workflow.dto.Edge;
import com.qworks.workflow.dto.Node;

import java.util.List;

public record TestWorkflowRequest(
        String workflowId,
        List<Node> nodes,
        List<Edge> edges
) { }
