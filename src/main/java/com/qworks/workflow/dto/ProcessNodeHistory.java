package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qworks.workflow.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessNodeHistory {

    private String nodeId;
    private ProcessStatus status;

    private String displayName;

    private String note;

    private Date startTime;

    private Date endTime;
}
