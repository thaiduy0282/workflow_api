package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowNodeConfigurationDto {

    @Id
    private String id;

    private String workflowId;

    private String displayName;

    private String nodeId;

    private WorkflowTriggerConfigurationDto triggerConfiguration;

    private WorkflowConditionExpressionDto condition;

    private WorkflowActionConfigurationDto action;

    private WorkflowErrorConfigurationDto errorConfiguration;

    private UUID createdBy;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;
}
