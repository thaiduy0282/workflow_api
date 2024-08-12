package com.qworks.workflow.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.qworks.workflow.entity.ProcessEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessRepository extends CosmosRepository<ProcessEntity, String> {

    Optional<ProcessEntity> findByProcessDefinitionId(String processDefinitionId);

    Optional<ProcessEntity> findByProcessInstanceId(String processInstanceId);

}
