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
public class AccountPayload {

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("messageNumber")
    private int messageNumber;

}
