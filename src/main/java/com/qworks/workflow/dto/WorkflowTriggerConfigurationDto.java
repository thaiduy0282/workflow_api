package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qworks.workflow.enums.EventCategory;
import com.qworks.workflow.enums.EventProvider;
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
