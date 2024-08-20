package com.qworks.workflow.mapper;

import com.qworks.workflow.dto.WorkflowNodeDto;
import com.qworks.workflow.entity.WorkflowNodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkflowNodeMapper {

    WorkflowNodeDto toWorkflowConditionDto(WorkflowNodeEntity workflowEntity);

    WorkflowNodeEntity toWorkflowConditionEntity(WorkflowNodeDto workflowDto);

}