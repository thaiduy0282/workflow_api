package com.qworks.workflow.dto;

import ai.qworks.dto.workflowapi.ProcessNodeHistory;
import ai.qworks.enums.workflowapi.ProcessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ProcessDto {

    private String id;

    private String workflowId;

    private String workflowName;

    private String processInstanceId;

    private ProcessStatus status;

    private String triggerBy;

    private List<ProcessNodeHistory> details;

    @CreatedDate
    private Date startTime;

    private Date endTime;

}
