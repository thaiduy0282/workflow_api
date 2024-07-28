package com.qworks.workflow.repository;

import com.qworks.workflow.entity.ProcessEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessRepository extends MongoRepository<ProcessEntity, String> {

    Optional<ProcessEntity> findByProcessDefinitionId(String processDefinitionId);

}
