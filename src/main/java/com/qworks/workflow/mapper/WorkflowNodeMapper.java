package com.qworks.workflow.mapper;

import ai.qworks.dao.nontransaction.workflowapi.WorkflowNodeEntity;
import com.qworks.workflow.dto.WorkflowNodeDto;
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