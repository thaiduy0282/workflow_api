package com.qworks.workflow.entity;

import com.qworks.workflow.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document(collection = "processes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProcessEntity {

    @Id
    private String id;

    private String processDefinitionId;

    private String workflowId;

    private ProcessStatus status;

    private UUID triggerBy;

    @CreatedDate
    private Date triggerTime;

    private Date endDate;
}
