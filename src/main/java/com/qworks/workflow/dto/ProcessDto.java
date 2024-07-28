package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qworks.workflow.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.UUID;

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

    private ProcessStatus status;

    private UUID triggerBy;

    @CreatedDate
    private Date triggerTime;

    private Integer timeSince;

}
