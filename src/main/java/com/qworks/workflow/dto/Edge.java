package com.qworks.workflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.qworks.workflow.constants.WorkflowConstants.NODE_ID_PREFIX;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Edge {
    private String id;
    private String source;
    private String target;
    private boolean animated;
    private LabelStyle labelStyle;
    private String type;
    private MarkerEnd markerEnd;
    private Style style;
}
