package com.qworks.workflow.entity;

import com.qworks.workflow.dto.WorkflowActionConfigurationDto;
import com.qworks.workflow.dto.WorkflowConditionExpressionDto;
import com.qworks.workflow.dto.WorkflowErrorConfigurationDto;
import com.qworks.workflow.dto.WorkflowTriggerConfigurationDto;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "node-configurations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class WorkflowNodeConfigurationEntity {

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
