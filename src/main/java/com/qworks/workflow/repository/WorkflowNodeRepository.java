package com.qworks.workflow.repository;

import ai.qworks.dao.nontransaction.workflowapi.WorkflowNodeEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends CosmosRepository<WorkflowNodeEntity, String> {

    List<WorkflowNodeEntity> findAll();

    void deleteAllByIdIn(List<String> ids);

    List<WorkflowNodeEntity> findByWorkflowIdAndNodeId(String workflowId, String nodeId);

    List<WorkflowNodeEntity> findByNodeId(String nodeId);

    List<WorkflowNodeEntity> findByWorkflowId(String workflowId);

    void deleteAllByWorkflowId(String workflowId);

    List<WorkflowNodeEntity> findByTriggerConfiguration_CategoryAndTriggerConfiguration_EventTopic(String category, String eventTopic);

}
