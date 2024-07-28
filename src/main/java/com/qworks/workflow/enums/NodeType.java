package com.qworks.workflow.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NodeType {

    @JsonProperty("StartEvent")
    START_EVENT,

    @JsonProperty("EndEvent")
    END_EVENT,
    @JsonProperty("Loop")
    LOOP,
    @JsonProperty("If")
    IF,

    @JsonProperty("Action")
    ACTION,

    @JsonProperty("action__group")
    ACTION_GROUP,

    @JsonProperty("ErrorHandler")
    ERROR_HANDLER
}
