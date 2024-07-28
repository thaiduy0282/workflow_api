package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.qworks.workflow.enums.WorkflowStatus;
import com.qworks.workflow.enums.WorkflowType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDto {

    private String id;

    private String name;

    private String processDefinitionId;

    private WorkflowType type;

    private WorkflowStatus status;

    private JsonNode nodes;

    private JsonNode edges;

    private UUID createdBy;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss.SS")
    private Date createdDate;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss.SS")
    private Date lastModifiedDate;

    private Map<String, String> links;

}
