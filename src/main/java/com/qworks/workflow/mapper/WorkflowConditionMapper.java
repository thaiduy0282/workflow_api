package com.qworks.workflow.mapper;

import com.qworks.workflow.dto.WorkflowNodeConfigurationDto;
import com.qworks.workflow.entity.WorkflowNodeConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkflowConditionMapper {

    WorkflowNodeConfigurationDto toWorkflowConditionDto(WorkflowNodeConfigurationEntity workflowEntity);

    WorkflowNodeConfigurationEntity toWorkflowConditionEntity(WorkflowNodeConfigurationDto workflowDto);

}