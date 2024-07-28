package com.qworks.workflow.repository;

import com.qworks.workflow.entity.WorkflowEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends MongoRepository<WorkflowEntity, String> {

    @NotNull
    Page<WorkflowEntity> findAll(@NotNull Pageable pageable);

    void deleteAllByIdIn(List<String> ids);

    Optional<WorkflowEntity> findByProcessDefinitionId(String processDefinitionId);

}
