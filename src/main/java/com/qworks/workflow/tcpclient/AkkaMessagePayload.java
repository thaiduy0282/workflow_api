package com.qworks.workflow.tcpclient;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AkkaMessagePayload {

    private String routingKey;
    private Properties properties;
    private JsonNode payload;

}