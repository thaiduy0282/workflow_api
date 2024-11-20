package com.qworks.workflow.dto;

import ai.qworks.enums.workflowapi.EventCategory;
import ai.qworks.enums.workflowapi.EventProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowTriggerConfigurationDto {

    private EventCategory category;

    private EventProvider provider;

    private String eventTopic;
}
