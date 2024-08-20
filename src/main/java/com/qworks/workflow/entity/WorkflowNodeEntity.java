package com.qworks.workflow.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.qworks.workflow.dto.WorkflowActionConfigurationDto;
import com.qworks.workflow.dto.WorkflowConditionExpressionDto;
import com.qworks.workflow.dto.WorkflowErrorConfigurationDto;
import com.qworks.workflow.dto.WorkflowTriggerConfigurationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@Container(containerName = "workflow-nodes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class WorkflowNodeEntity {

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
