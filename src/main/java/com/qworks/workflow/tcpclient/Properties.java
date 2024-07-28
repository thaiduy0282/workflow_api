package com.qworks.workflow.tcpclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Properties {

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("delivery_mode")
    private int deliveryMode;

}
