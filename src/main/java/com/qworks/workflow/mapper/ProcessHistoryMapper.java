package com.qworks.workflow.mapper;

import com.qworks.workflow.dto.ProcessHistoryDto;
import com.qworks.workflow.entity.ProcessHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessHistoryMapper {

    ProcessHistoryDto toDto(ProcessHistoryEntity processHistoryEntity);

    ProcessHistoryEntity toEntity(ProcessHistoryDto processDto);

}
