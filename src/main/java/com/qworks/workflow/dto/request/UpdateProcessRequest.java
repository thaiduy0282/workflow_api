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

        Optional<Date> startTime,

        Optional<Date> endTime,

        Optional<String> note

) { }
