package com.qworks.workflow.mapper;

import com.qworks.workflow.dto.WorkflowConfigurationDto;
import com.qworks.workflow.entity.WorkflowConfigurationEntity;
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