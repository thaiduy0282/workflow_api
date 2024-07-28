package com.qworks.workflow.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.dto.WorkflowDto;
import com.qworks.workflow.entity.WorkflowEntity;
import com.qworks.workflow.exception.SystemErrorException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class WorkflowMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(source = "nodes", target = "nodes", qualifiedByName = "jsonNodeToString")
    @Mapping(source = "edges", target = "edges", qualifiedByName = "jsonNodeToString")
    public abstract WorkflowEntity toWorkflowEntity(WorkflowDto dto);

    @Mapping(source = "nodes", target = "nodes", qualifiedByName = "stringToJsonNode")
    @Mapping(source = "edges", target = "edges", qualifiedByName = "stringToJsonNode")
    public abstract WorkflowDto toWorkflowDto(WorkflowEntity entity);

    @Named("jsonNodeToString")
    protected String jsonNodeToString(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new SystemErrorException("Error converting JsonNode to String");
        }
    }

    @Named("stringToJsonNode")
    protected JsonNode stringToJsonNode(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            throw new SystemErrorException("Error converting String to JsonNode");
        }
    }
}
