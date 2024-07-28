package com.qworks.workflow.repository;

import com.qworks.workflow.entity.WorkflowNodeConfigurationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeConfigurationRepository extends MongoRepository<WorkflowNodeConfigurationEntity, String> {
    void deleteAllByIdIn(List<String> ids);

    List<WorkflowNodeConfigurationEntity> findByWorkflowIdAndNodeId(String workflowId, String nodeId);

    List<WorkflowNodeConfigurationEntity> findByWorkflowId(String workflowId);

    void deleteAllByWorkflowId(String workflowId);

    List<WorkflowNodeConfigurationEntity> findByTriggerConfigurationCategoryAndTriggerConfigurationProviderAndTriggerConfigurationEventTopic(
            String category, String provider, String eventTopic);

}
