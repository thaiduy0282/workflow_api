package com.qworks.workflow.mapper;

import ai.qworks.dao.nontransaction.workflowapi.WorkflowConfigurationEntity;
import com.qworks.workflow.dto.WorkflowConfigurationDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkflowConfigurationMapper {

    WorkflowConfigurationDto toWorkflowConfigurationDto(WorkflowConfigurationEntity workflowEntity);

    WorkflowConfigurationEntity toWorkflowConfigurationEntity(WorkflowConfigurationDto workflowDto);

}