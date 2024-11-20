package com.qworks.workflow.mapper;

import ai.qworks.dao.nontransaction.workflowapi.ProcessEntity;
import com.qworks.workflow.dto.ProcessDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessMapper {

    ProcessDto toDto(ProcessEntity processEntity);

    ProcessEntity toEntity(ProcessDto processDto);

}
