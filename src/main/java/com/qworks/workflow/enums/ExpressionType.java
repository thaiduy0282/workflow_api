package com.qworks.workflow.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExpressionType {
    @JsonProperty("string")
    STRING,
    @JsonProperty("mathematics")
    MATHEMATICS
}