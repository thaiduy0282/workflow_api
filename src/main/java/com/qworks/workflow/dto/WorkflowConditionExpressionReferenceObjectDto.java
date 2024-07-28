package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowConditionExpressionReferenceObjectDto {

    private String label;
    private String name;
    private String apiName;
    private String value;
    private String fieldType;
    private String fieldDataType;
    private String description = null;
    private float fieldLength;
    private float sequenceNumber;
    private String required = null;
    private String defaultValue = null;
    List<Object> validations = new ArrayList<>();
    private boolean editable;
    private boolean visible;
    List<Object> options = new ArrayList<>();
}
