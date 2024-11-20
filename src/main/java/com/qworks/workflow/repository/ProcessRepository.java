package com.qworks.workflow.repository;

import ai.qworks.dao.nontransaction.workflowapi.ProcessEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessRepository extends CosmosRepository<ProcessEntity, String> {

    Optional<ProcessEntity> findByProcessDefinitionId(String processDefinitionId);

    Optional<ProcessEntity> findByProcessInstanceId(String processInstanceId);

}
