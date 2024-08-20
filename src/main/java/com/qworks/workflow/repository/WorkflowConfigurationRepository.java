package com.qworks.workflow.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.qworks.workflow.entity.WorkflowConfigurationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowConfigurationRepository extends CosmosRepository<WorkflowConfigurationEntity, String> {

    List<WorkflowConfigurationEntity> findAll();
}
