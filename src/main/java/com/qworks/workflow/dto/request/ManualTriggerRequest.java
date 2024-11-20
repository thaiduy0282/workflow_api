package com.qworks.workflow.dto.request;

import lombok.Builder;

@Builder
public record ManualTriggerRequest(

        String object,
        long lastModifiedDate

) { }
