package com.qworks.workflow.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.qworks.workflow.entity.WorkflowNodeConfigurationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeConfigurationRepository extends CosmosRepository<WorkflowNodeConfigurationEntity, String> {

    List<WorkflowNodeConfigurationEntity> findAll();

    void deleteAllByIdIn(List<String> ids);

    List<WorkflowNodeConfigurationEntity> findByWorkflowIdAndNodeId(String workflowId, String nodeId);

    List<WorkflowNodeConfigurationEntity> findByNodeId(String nodeId);

    List<WorkflowNodeConfigurationEntity> findByWorkflowId(String workflowId);

    void deleteAllByWorkflowId(String workflowId);

    List<WorkflowNodeConfigurationEntity> findByTriggerConfigurationCategoryAndTriggerConfigurationProviderAndTriggerConfigurationEventTopic(
            String category, String provider, String eventTopic);

}
