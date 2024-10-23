package com.qworks.workflow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qworks.workflow.constants.WorkflowConstants;
import com.qworks.workflow.dto.Data;
import com.qworks.workflow.dto.Edge;
import com.qworks.workflow.dto.Node;
import com.qworks.workflow.dto.WorkflowDto;
import com.qworks.workflow.dto.request.CreateWorkflowRequest;
import com.qworks.workflow.dto.request.ManualTriggerRequest;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.dto.request.UpdateWorkflowRequest;
import com.qworks.workflow.entity.WorkflowEntity;
import com.qworks.workflow.enums.NodeType;
import com.qworks.workflow.enums.WorkflowStatus;
import com.qworks.workflow.exception.BPMNException;
import com.qworks.workflow.exception.NodeTypeNotSupport;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.exception.SystemErrorException;
import com.qworks.workflow.helper.LinkHelper;
import com.qworks.workflow.mapper.WorkflowMapper;
import com.qworks.workflow.repository.WorkflowRepository;
import com.qworks.workflow.service.ProcessService;
import com.qworks.workflow.service.WorkflowNodeService;
import com.qworks.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.dto.DeploymentWithDefinitionsDto;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

import static com.qworks.workflow.constants.WorkflowConstants.ALLOWED_TRIGGER_OBJECTS;
import static com.qworks.workflow.constants.WorkflowConstants.DATA;
import static com.qworks.workflow.constants.WorkflowConstants.NODE_ID_PREFIX;
import static com.qworks.workflow.util.RestTemplateUtil.getHttpHeaders;
import static com.qworks.workflow.util.RestTemplateUtil.getRequestFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    @Value("${qworks.baseUrl}")
    private String baseUrl;
    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    private final ObjectMapper objectMapper;
    private final DeploymentApi deploymentApi;

    private final WorkflowNodeService workflowNodeService;

    private final ProcessService processService;


    @Override
    public Page<WorkflowDto> findAll(Pageable pageable) {
        return workflowRepository.findAll(pageable)
                .map(workflowMapper::toWorkflowDto)
                .map(workflowDto -> workflowDto.setLinks(LinkHelper.createLinks(workflowDto.getId())));
    }


    @Override
    public WorkflowDto findById(String id) {
        WorkflowEntity workflowEntity = getWorkflowById(id);
        return workflowMapper.toWorkflowDto(workflowEntity);
    }

    private WorkflowEntity getWorkflowById(String id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(WorkflowConstants.WORKFLOW_NOT_FOUND + id));
    }

    @Override
    public WorkflowDto create(CreateWorkflowRequest request) {
        WorkflowDto workflowDto = new WorkflowDto();
        workflowDto.setId(UUID.randomUUID().toString());
        request.name().ifPresent(workflowDto::setName);
        request.type().ifPresent(workflowDto::setType);
        request.createdBy().ifPresent(workflowDto::setCreatedBy);
        workflowDto.setNodes(request.nodes());
        workflowDto.setEdges(request.edges());

        WorkflowEntity workflowEntity = workflowMapper.toWorkflowEntity(workflowDto);
        workflowEntity.setNodes(convertToJsonString(request.nodes()));
        workflowEntity.setEdges(convertToJsonString(request.edges()));
        workflowEntity.setCreatedDate(new Date());
        workflowEntity.setLastModifiedDate(new Date());
        workflowEntity.setStatus(WorkflowStatus.DRAFT);
        workflowEntity = workflowRepository.save(workflowEntity);
        return workflowMapper.toWorkflowDto(workflowEntity);
    }

    @Override
    public WorkflowDto update(String id, UpdateWorkflowRequest dto) {
        WorkflowEntity existingWorkflow = getWorkflowById(id);

        dto.name().ifPresent(existingWorkflow::setName);
        dto.type().ifPresent(existingWorkflow::setType);
        dto.nodes().ifPresent(nodes -> existingWorkflow.setNodes(convertToJsonString(nodes)));
        dto.edges().ifPresent(edges -> existingWorkflow.setEdges(convertToJsonString(edges)));
        existingWorkflow.setLastModifiedDate(new Date());
        WorkflowEntity savedWorkflow = workflowRepository.save(existingWorkflow);
        return workflowMapper.toWorkflowDto(savedWorkflow);
    }

    @Override
    public void delete(String id) {
        workflowNodeService.deleteByWorkflowId(id);
        workflowRepository.deleteById(id);
    }

    @Override
    public void batchDelete(List<String> ids) {
        workflowRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional
    public File generateBPMNProcess(WorkflowDto workflowDto, List<Node> nodes, List<Edge> edges) throws Exception {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("executableProcess").executable().done();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        String processInstanceId = "definition_" + workflowDto.getId();
        Process process = createElement(definitions, processInstanceId, Process.class);
        process.setExecutable(true);
        process.setCamundaHistoryTimeToLiveString("180");

        Map<String, Node> nodeMap = new HashMap<>();
        Map<String, FlowNode> flowNodeMap = new HashMap<>();

        nodes.forEach(node -> {
            nodeMap.put(node.getId(), node);
            NodeType nodeType = node.getData().getTypeNode();
            String endStepId = NODE_ID_PREFIX + UUID.randomUUID();
            switch (nodeType) {
                case START_EVENT:
                    String startActivityName = "Start";
                    StartEvent startEventNode = createElement(process, node.getId(), StartEvent.class);
                    startEventNode.setName(startActivityName);
                    startEventNode.setParallelMultiple(true);
                    startEventNode.setInterrupting(false);
                    addExecutionListener(startEventNode);
                    flowNodeMap.put(node.getId(), startEventNode);
                    break;
                case END_EVENT:
                    String preEndStepActivityName = "preComplete step";
                    ServiceTask preEndStep = createElement(process, node.getId(), ServiceTask.class);
                    preEndStep.setCamundaType("external");
                    preEndStep.setCamundaTopic("pre_complete");
                    preEndStep.setName(preEndStepActivityName);
                    addExecutionListener(preEndStep);
                    flowNodeMap.put(node.getId(), preEndStep);
                    String endActivityName = "End";

                    EndEvent endEventNode = createElement(process, endStepId, EndEvent.class);
                    endEventNode.setName(endActivityName);
                    Edge edge = new Edge();
                    edge.setSource(node.getId());
                    edge.setTarget(endStepId);
                    edges.add(edge);

                    addExecutionListener(endEventNode);
                    flowNodeMap.put(endStepId, endEventNode);
                    break;
                case IF:
                    String validationFilterActivityName = "validation_filter";
                    ServiceTask conditionNode = createElement(process, node.getId(), ServiceTask.class);
                    conditionNode.setCamundaType("external");
                    conditionNode.setCamundaTopic("validation_filter");
                    conditionNode.setName(validationFilterActivityName);
                    addExecutionListener(conditionNode);
                    flowNodeMap.put(node.getId(), conditionNode);
                    break;
                case ACTION:
                    String actionActivityName = "action_task";
                    ServiceTask actionNode = createElement(process, node.getId(), ServiceTask.class);
                    actionNode.setCamundaType("external");
                    actionNode.setCamundaTopic("action_task");
                    actionNode.setName(actionActivityName);
                    addExecutionListener(actionNode);
                    flowNodeMap.put(node.getId(), actionNode);
                    break;
                case ERROR_HANDLER:
                    String errorHandlerActivityName = "error_handler";
                    ServiceTask errorHandlerNode = createElement(process, node.getId(), ServiceTask.class);
                    errorHandlerNode.setCamundaType("external");
                    errorHandlerNode.setCamundaTopic("error_handler");
                    errorHandlerNode.setName(errorHandlerActivityName);
                    addExecutionListener(errorHandlerNode);
                    flowNodeMap.put(node.getId(), errorHandlerNode);
                    break;
                case LOOP:
                    FlowNode loopNode = createElement(process, node.getId(), FlowNode.class);
                    flowNodeMap.put(node.getId(), loopNode);
                    break;
                default:
                    throw new NodeTypeNotSupport("Node Type not support!");
            }
        });

        edges.forEach(edge -> {
            if (flowNodeMap.containsKey(edge.getSource()) && flowNodeMap.containsKey(edge.getTarget())) {
                createSequenceFlow(process, flowNodeMap, nodeMap, edge.getSource(), edge.getTarget());
            } else {
                log.info("Edge source or target not found: {} -> {}", edge.getSource(), edge.getTarget());
            }
        });

        File file = null;
        try {
            Bpmn.validateModel(modelInstance);
            file = File.createTempFile("bpmn_process", ".bpmn");
            Bpmn.writeModelToFile(file, modelInstance);
        } catch (BpmnModelException e) {
            log.error("Occurred error while writing model to file: {}", e.getMessage());
            throw new BPMNException("Occurred error while writing model to file: " + e.getMessage());
        } catch (ModelValidationException e) {
            log.error("Occurred error while validating model: {}", e.getMessage());
            throw new BPMNException("Occurred error while writing model to file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Occurred error while generating BPMN model: {}", e.getMessage());
            throw new BPMNException("Occurred error while writing model to file: " + e.getMessage());
        }

        workflowDto.setProcessDefinitionId(processInstanceId);
        return file;
    }

    private void addExecutionListener(FlowNode flowNode) {
        List<String> events = Arrays.asList("start", "end");
        events.forEach(event -> {
            CamundaExecutionListener listener = flowNode.getModelInstance().newInstance(CamundaExecutionListener.class);
            listener.setCamundaEvent(event);
            listener.setCamundaDelegateExpression("#{taskExecutionListener}");

            ExtensionElements extensionElements = flowNode.getExtensionElements();
            if (extensionElements == null) {
                extensionElements = flowNode.getModelInstance().newInstance(ExtensionElements.class);
                flowNode.setExtensionElements(extensionElements);
            }

            extensionElements.addChildElement(listener);
        });
    }

    @Override
    public void publishWorkflow(String workflowId, boolean isPublished) throws BPMNException {
        WorkflowDto workflowDto = findById(workflowId);
        if (workflowDto == null) {
            throw new ResourceNotFoundException("Workflow not found");
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Node> nodes = mapper.convertValue(workflowDto.getNodes(), new TypeReference<>() {});
        List<Edge> edges = mapper.convertValue(workflowDto.getEdges(), new TypeReference<>() {});

        File file = null;
        File outputFile = null;
        try {
            file = generateBPMNProcess(workflowDto, nodes, edges);
            outputFile = BPMNDiagramGenerator.generateBPMNDiagram(file);
        } catch (Exception e) {
            log.error("Occurred error while generating the BPMN diagram: " + workflowId, e);
            throw new BPMNException("Occurred error while generating the BPMN diagram: " + workflowId);
        }

        DeploymentWithDefinitionsDto response = null;
        try {
            log.info("prepare to call createDeployment...");
            response = deploymentApi.createDeployment(null, "QWorks Workflow App", true, true, workflowDto.getName(), new Date(), outputFile);
        } catch (ApiException e) {
            log.error("Occurred error while deploying BPMN diagram to BPMN serve: " + workflowId, e);
            throw new BPMNException("Occurred error while deploying BPMN diagram to BPMN serve: " + workflowId);
        }

        if (response == null) {
            log.error("Occurred error while deploying BPMN diagram to BPMN serve: " + workflowId);
            throw new BPMNException("Occurred error while deploying BPMN diagram to BPMN serve: " + workflowId);
        }

        log.info("Workflow is published successfully!!" + workflowId);
        workflowDto.setStatus(WorkflowStatus.PUBLISHED);
        saveWorkflow(workflowDto);
    }

    @Override
    public List<String> manualTrigger(ManualTriggerRequest request) {
        List<String> processesIds = new ArrayList<>();
        if (ALLOWED_TRIGGER_OBJECTS.contains(request.object())) {
            JsonNode apiResponse = callApiWithTimestamp(request.object(), request.lastModifiedDate());
            if (apiResponse != null) {
                List<String> configurations = workflowNodeService.getUniqueWorkflowIdBaseOnTriggerObject(request.object());
                if (configurations.isEmpty()) {
                    log.info("No workflows are configured with the trigger object {} then skipped it", request.object());
                    return processesIds;
                }

                log.info("Found {} workflows with trigger object {}: {}", configurations.size(), request.object(), StringUtils.join(configurations));
                configurations.forEach(workflowId -> {
                    processesIds.addAll(triggerProcess(workflowId, request.object(), apiResponse));
                });
            }
        } else {
            log.info("The workflow app does not support this trigger object {} for {}", request.object(), request.lastModifiedDate());
        }

        return processesIds;
    }

    @Override
    public void deleteAllWorkflows() {
        workflowRepository.deleteAll();
        workflowNodeService.deleteAllNodes();
    }

    private JsonNode callApiWithTimestamp(String triggerObject, long timestamp) {
        log.info("Fetching the list of {} objects that were updated within the specified timestamp: {}", triggerObject, timestamp);
        String apiUrl = baseUrl + "metabench/fetchRecords/" + triggerObject;
        RestTemplate restTemplate = new RestTemplate(getRequestFactory());
        try {
            HttpHeaders headers = getHttpHeaders();
            String json = "{ \"lastmodifieddate\": " + timestamp + " }";
            HttpEntity<String> entity = new HttpEntity<String>(json, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, JsonNode.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("API response: " + response.getBody());
                return response.getBody();
            } else {
                log.error("Unexpected code {} returning while calling the API to fetch {} objects updated within the specified timestamp: {} ", response.getStatusCode(), triggerObject, timestamp);
            }
        } catch(HttpStatusCodeException e) {
            log.error("An error occurred while calling the API to fetch {} objects updated within the specified timestamp: {}", triggerObject, timestamp, e);
        }

        return null;
    }

    private List<String> triggerProcess(String workflowId, String object, JsonNode apiResponse) {
        List<String> processesIds = new ArrayList<>();
        try {
            JsonNode dataArrNode = apiResponse.path(DATA);
            if (dataArrNode.isArray() && dataArrNode.size() > 0) {
                for (JsonNode childNode : dataArrNode) {
                    Map<String, VariableValueDto> variables = new HashMap<>();
                    variables.put(StringUtils.lowerCase(object), new VariableValueDto().value(childNode.toString()).type("string"));
                    log.info("Executing workflow {} with data: {}", workflowId, childNode.toString());
                    ProcessInstanceWithVariablesDto res = processService.triggerProcess("definition_" + workflowId, variables, new TriggerProcessRequest(""));
                    processesIds.add(res.getId());
                }
            } else {
                log.info("No data was returned from the QWorks system to initiate the workflow {}", workflowId);
            }
        } catch (Exception e) {
            log.error("Error initiating process for workflowId {}: ", workflowId, e);
        }

        return processesIds;
    }


    private void saveWorkflow(WorkflowDto workflowDto) {
        WorkflowEntity workflowEntity = workflowMapper.toWorkflowEntity(workflowDto);
        workflowRepository.save(workflowEntity);
    }

    protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private SequenceFlow createSequenceFlow(Process process, Map<String, FlowNode> flowNodeMap, Map<String, Node> nodeMap,
                                            String sourceId, String targetId) {
        FlowNode from = flowNodeMap.get(sourceId);
        FlowNode to = flowNodeMap.get(targetId);
        String identifier = from.getId() + "-" + to.getId();
        SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);

        // add the condition in the edge flow
        Node toNode = nodeMap.get(targetId);
        if (toNode != null) {
            Data data = toNode.getData();
            if (BooleanUtils.isTrue(data.getIsAfterConditionNode())) {
                ConditionExpression conditionExpression = createElement(sequenceFlow, identifier + "_condition", ConditionExpression.class);
                if (BooleanUtils.isTrue(data.getIsYesCase())) {
                    conditionExpression.setTextContent("${isTrue}");
                    sequenceFlow.setName("Yes");
                } else if (BooleanUtils.isTrue(data.getIsNoCase())) {
                    conditionExpression.setTextContent("${!isTrue}");
                    sequenceFlow.setName("No");
                }

                sequenceFlow.addChildElement(conditionExpression);
            }

        }

        return sequenceFlow;
    }

    private String convertToJsonString(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new SystemErrorException("Error converting JsonNode to String");
        }
    }
}
