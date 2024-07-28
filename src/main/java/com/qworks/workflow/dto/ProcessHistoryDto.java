package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qworks.workflow.enums.ProcessHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
@ToString
public class ProcessHistoryDto {

    private String id;

    private String activityId;

    private String activityName;

    private String processDefinitionId;

    private String workflowId;

    private ProcessHistoryStatus status;

    private String description;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss.SS")
    private Date startDate;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss.SS")
    private Date endDate;

}