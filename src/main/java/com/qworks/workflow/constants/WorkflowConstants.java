package com.qworks.workflow.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowConstants {

    public static final String WORKFLOW_NOT_FOUND = "Workflow not found with id: ";
    public static final String NODE_ID_PREFIX = "id_";

    public static final String ADMIN_USER = "Admin";

    public static final String END_NODE = "End";

    public static final String ACTION_UPDATE = "Update";

    public static final String ACTION_GET = "Get";

    public static final String ACCOUNT = "ACCOUNT";

    public static final String CONTRACT = "CONTRACT";

    public static final String QUOTE = "QUOTE";

    public static final String ID = "id";

    public static final String DATA = "data";

    public static final List<String> ALLOWED_TRIGGER_OBJECTS = Arrays.asList(ACCOUNT, CONTRACT, QUOTE);

}
