package com.qworks.workflow.entity;

import com.qworks.workflow.enums.ProcessHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "processHistories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProcessHistoryEntity {

    @Id
    private String id;

    private String activityId;

    private String activityName;

    private String processDefinitionId;

    private ProcessHistoryStatus status;

    private String description;

    private Date startDate;

    private Date endDate;

}
