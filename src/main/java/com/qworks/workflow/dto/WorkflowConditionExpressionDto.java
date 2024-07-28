package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qworks.workflow.enums.ExpressionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowConditionExpressionDto {

    private String expression;

    private List<String> referenceObjects;
}