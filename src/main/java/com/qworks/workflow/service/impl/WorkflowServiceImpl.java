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
import com.qworks.workflow.dto.request.UpdateWorkflowRequest;
import com.qworks.workflow.entity.WorkflowEntity;
import com.qworks.workflow.enums.NodeType;
import com.qworks.workflow.enums.WorkflowStatus;
import com.qworks.workflow.exception.NodeTypeNotSupport;
import com.qworks.workflow.exception.ResourceNotFoundException;
import com.qworks.workflow.exception.SystemErrorException;
import com.qworks.workflow.helper.LinkHelper;
import com.qworks.workflow.mapper.WorkflowMapper;
import com.qworks.workflow.repository.WorkflowRepository;
import com.qworks.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
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
import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.dto.DeploymentWithDefinitionsDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.qworks.workflow.constants.WorkflowConstants.NODE_ID_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    private final ObjectMapper objectMapper;
    private final DeploymentApi deploymentApi;

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
        request.name().ifPresent(workflowDto::setName);
        request.type().ifPresent(workflowDto::setType);
        request.createdBy().ifPresent(workflowDto::setCreatedBy);
        workflowDto.setNodes(request.nodes());
        workflowDto.setEdges(request.edges());

        WorkflowEntity workflowEntity = workflowMapper.toWorkflowEntity(workflowDto);
        workflowEntity.setNodes(convertToJsonString(request.nodes()));
        workflowEntity.setEdges(convertToJsonString(request.edges()));
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

        WorkflowEntity savedWorkflow = workflowRepository.save(existingWorkflow);
        return workflowMapper.toWorkflowDto(savedWorkflow);
    }

    @Override
    public void delete(String id) {
        WorkflowEntity existingWorkflow = getWorkflowById(id);
        workflowRepository.deleteById(existingWorkflow.getId());
    }

    @Override
    public void batchDelete(List<String> ids) {
        workflowRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional
    public File generateBPMNProcess(WorkflowDto workflowDto, List<Node> nodes, List<Edge> edges) throws IOException {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("test").executable().done();
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
                    addExecutionListener(startEventNode, "start");
                    addExecutionListener(startEventNode, "end");
                    flowNodeMap.put(node.getId(), startEventNode);
                    break;
                case END_EVENT:
                    String preEndStepActivityName = "preEnd step";
                    ServiceTask preEndStep = createElement(process, node.getId(), ServiceTask.class);
                    preEndStep.setCamundaType("external");
                    preEndStep.setCamundaTopic("pre_complete");
                    preEndStep.setName(preEndStepActivityName);
                    addExecutionListener(preEndStep, "start");
                    addExecutionListener(preEndStep, "end");
                    flowNodeMap.put(node.getId(), preEndStep);
                    String endActivityName = "End";
                    EndEvent endEventNode = createElement(process, endStepId, EndEvent.class);
                    endEventNode.setName(endActivityName);
                    Edge edge = new Edge();
                    edge.setSource(node.getId());
                    edge.setTarget(endStepId);
                    edges.add(edge);

                    addExecutionListener(endEventNode, "start");
                    addExecutionListener(endEventNode, "end");
                    flowNodeMap.put(endStepId, endEventNode);
                    break;
                case IF:
                    String validationFilterActivityName = "validation_filter";
                    ServiceTask conditionNode = createElement(process, node.getId(), ServiceTask.class);
                    conditionNode.setCamundaType("external");
                    conditionNode.setCamundaTopic("validation_filter");
                    conditionNode.setName(validationFilterActivityName);
                    addExecutionListener(conditionNode, "start");
                    addExecutionListener(conditionNode, "end");
                    flowNodeMap.put(node.getId(), conditionNode);
                    break;
                case ACTION:
                    String actionActivityName = "action_task";
                    ServiceTask actionNode = createElement(process, node.getId(), ServiceTask.class);
                    actionNode.setCamundaType("external");
                    actionNode.setCamundaTopic("action_task");
                    actionNode.setName(actionActivityName);
                    addExecutionListener(actionNode, "start");
                    addExecutionListener(actionNode, "end");
                    flowNodeMap.put(node.getId(), actionNode);
                    break;
                case ERROR_HANDLER:
                    String errorHandlerActivityName = "error_handler";
                    ServiceTask errorHandlerNode = createElement(process, node.getId(), ServiceTask.class);
                    errorHandlerNode.setCamundaType("external");
                    errorHandlerNode.setCamundaTopic("error_handler");
                    errorHandlerNode.setName(errorHandlerActivityName);
                    addExecutionListener(errorHandlerNode, "start");
                    addExecutionListener(errorHandlerNode, "end");
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

        Bpmn.validateModel(modelInstance);
        File file = File.createTempFile("bpmn_process", ".bpmn");
        Bpmn.writeModelToFile(file, modelInstance);

        workflowDto.setProcessDefinitionId(processInstanceId);
        return file;
    }

    private void addExecutionListener(FlowNode flowNode, String event) {
        CamundaExecutionListener listener = flowNode.getModelInstance().newInstance(CamundaExecutionListener.class);
        listener.setCamundaEvent(event);
        listener.setCamundaDelegateExpression("#{taskExecutionListener}");

        ExtensionElements extensionElements = flowNode.getExtensionElements();
        if (extensionElements == null) {
            extensionElements = flowNode.getModelInstance().newInstance(ExtensionElements.class);
            flowNode.setExtensionElements(extensionElements);
        }

        extensionElements.addChildElement(listener);
    }

    @Override
    public void publishWorkflow(String workflowId, boolean isPublished) throws IOException, ApiException {
        try {
            WorkflowDto workflowDto = findById(workflowId);
            if (workflowDto == null) {
                throw new ResourceNotFoundException("Workflow not found");
            }

            ObjectMapper mapper = new ObjectMapper();
            List<Node> nodes = mapper.convertValue(workflowDto.getNodes(), new TypeReference<>() {
            });
            List<Edge> edges = mapper.convertValue(workflowDto.getEdges(), new TypeReference<>() {
            });

            File file = generateBPMNProcess(workflowDto, nodes, edges);
            File outputFile = BPMNDiagramGenerator.generateBPMNDiagram(file);

            log.info("prepare to call createDeployment...");
            DeploymentWithDefinitionsDto response = deploymentApi.createDeployment(null, "QWorks Workflow App", true, true, workflowDto.getName(), new Date(), outputFile);
            log.info("call done!");

            if (response != null) {
                log.info("response not null");
                log.info("id: {}", response.getId());
                workflowDto.setStatus(WorkflowStatus.PUBLISHED);
                saveWorkflow(workflowDto);
            } else {
                log.info("response null!!!!!");
            }
        } catch (ApiException e) {
            log.error(e.getMessage());
        }
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
