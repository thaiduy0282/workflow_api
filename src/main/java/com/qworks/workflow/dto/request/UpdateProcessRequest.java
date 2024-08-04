package com.qworks.workflow.dto.request;

import com.qworks.workflow.enums.ProcessStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Date;
import java.util.Optional;

@Builder
public record UpdateProcessRequest(

        String processInstanceId,

        String nodeId,

        String activityName,

        @NotNull
        ProcessStatus status,

        Optional<Date> startDate,

        Optional<Date> endDate,

        Optional<String> note

) { }
