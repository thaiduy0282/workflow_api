package com.qworks.workflow.dto.request;

import com.qworks.workflow.enums.ProcessHistoryStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.Optional;

public record UpdateProcessHistoryRequest(

        @NotNull
        ProcessHistoryStatus status,

        Optional<Date> startDate,

        Optional<Date> endDate,

        Optional<String> description

) { }
