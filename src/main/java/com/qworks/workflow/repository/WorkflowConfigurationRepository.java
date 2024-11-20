package com.qworks.workflow.repository;

import ai.qworks.dao.nontransaction.workflowapi.WorkflowConfigurationEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowConfigurationRepository extends CosmosRepository<WorkflowConfigurationEntity, String> {

    List<WorkflowConfigurationEntity> findAll();
}
