package com.qworks.workflow.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.qworks.workflow.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Data {

    private String displayName;

    private String actionType;

    private String label;

    private NodeType typeNode;

    private Object nodes;

    private Object edges;

    private String category;

    private String provider;

    private String eventTopic;

    private Boolean isTrueNode;

    private Boolean isFalseNode;

    private Boolean isNoCase;

    private Boolean showSyntax;

    private Boolean isYesCase;

    private String step;

    private Boolean isAfterConditionNode;

    private String parentId;

    private List<JsonNode> formula;

    private List<JsonNode> formulaItems;

    private String expression;

    private String object;

    private List<JsonNode> fields;

    private List<JsonNode> referenceObjects;
}
