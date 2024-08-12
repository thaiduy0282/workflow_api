package com.qworks.workflow.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.qworks.workflow.enums.WorkflowStatus;
import com.qworks.workflow.enums.WorkflowType;
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

@Container(containerName = "workflows")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class WorkflowEntity {

    @Id
    private String id;

    private String name;

    private WorkflowType type;

    private WorkflowStatus status;

    private String nodes; // Store as JSON string

    private String edges; // Store as JSON string

    private UUID createdBy;

    private String processDefinitionId;

    @CreatedDate
    private Date createdDate;

    @LastModifiedDate
    private Date lastModifiedDate;
}
