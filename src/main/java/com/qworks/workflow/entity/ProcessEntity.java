package com.qworks.workflow.entity;

import com.qworks.workflow.dto.ProcessNodeHistory;
import com.qworks.workflow.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import com.azure.spring.data.cosmos.core.mapping.Container;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Container(containerName = "processes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProcessEntity {

    @Id
    private String id;

    private String processDefinitionId;

    private String processInstanceId;

    private String workflowId;
    private String workflowName;

    private ProcessStatus status;

    private String triggerBy;

    private List<ProcessNodeHistory> details;

    @CreatedDate
    private Date startTime;

    private Date endTime;

    private Date endDate;
}
