package com.qworks.workflow.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.qworks.workflow.entity.WorkflowEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends CosmosRepository<WorkflowEntity, String> {

    @NotNull
    Page<WorkflowEntity> findAll(@NotNull Pageable pageable);

    void deleteAllByIdIn(List<String> ids);

    Optional<WorkflowEntity> findByProcessDefinitionId(String processDefinitionId);

}
