package com.qworks.workflow.controller;

import com.qworks.workflow.dto.ProcessDto;
import com.qworks.workflow.dto.request.TriggerProcessRequest;
import com.qworks.workflow.dto.request.UpdateProcessRequest;
import com.qworks.workflow.dto.response.ApiResponse;
import com.qworks.workflow.entity.ProcessEntity;
import com.qworks.workflow.exception.model.SuccessResponse;
import com.qworks.workflow.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final ProcessService processService;

    @PostMapping("/trigger/{processName}")
    public ResponseEntity<SuccessResponse> triggerWorkflow(@PathVariable String processName, @RequestBody TriggerProcessRequest request) throws ApiException {
        //This method is using for testing purpose only
        Map<String, VariableValueDto> variables = new HashMap<>();
        //Handle event for account event
        String jsonRecord = "{ \"annualrevenue\": 4545654.0, \"employeescount\": 55, \"isactive\": true, \"islatest\": true, \"createddate\": 1715947270671, \"lastmodifieddate\": 1715947695805, \"businessactivities\": \"Activities..\", \"communicationpreferences\": \"Preferences!!\", \"accountlogourl\": \"https://account.com\", \"accountmanager\": null, \"accountnumber\": \"123456\", \"accountsource\": \"Referral\", \"accountstatus\": null, \"accounttype\": \"Partner\", \"accountwebsite\": \"https://account.com\", \"createdby\": null, \"credithold\": \"Billing\", \"note\": \"Account1 - appended note from API\", \"id\": \"75b85915-bf80-4fb7-8b85-ee3ce9128f5a\", \"industry\": \"Agriculture\", \"jurisdiction\": \"CA\", \"lastmodifiedby\": \"naresh@qworks.ai\", \"name\": \"Account1\", \"ownerid\": \"3999ac44-cb85-4931-bf49-d7b10e8bc6fc\", \"parentaccountid\": \"54b16c4a-a589-4f88-985b-0468f75d5c7d\", \"partnertype\": \"Direct Reseller\", \"qwextid\": null, \"shippinghold\": \"New Order\", \"status\": \"account_status_active\", \"tags\": \"[]\", \"taxidregistrationnumber\": \"4589-545\", \"tenantid\": null, \"ownername\": \"Naresh Bachu\", \"parentaccountname\": \"Microsoft\", \"customfields\": null, \"isroot\": null, \"territoryid\": null }";
        variables.put("triggerData", new VariableValueDto().value(jsonRecord).type("string"));
        this.processService.triggerProcess(processName, variables, request);

        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workflow is triggered successfully..!")
                .data(processName)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessDto> getProcessById(@PathVariable String id) {
        return processService.getProcessById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProcessDto>> getAllProcesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sort = Sort.by(Sort.Order.desc("startTime"));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProcessDto> processDtos = processService.findAll(pageable);
        ApiResponse<ProcessDto> response = new ApiResponse<>(
                processDtos.getTotalElements(),
                size,
                page,
                processDtos.getTotalPages(),
                processDtos.getContent()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteProcess(@PathVariable String id) {
        processService.deleteProcess(id);
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Process data was deleted successfully..!")
                .data(id)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<SuccessResponse> deleteAllProcesses() {
        processService.deleteAllProcesses();
        SuccessResponse response = SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("All process data was deleted successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<ProcessEntity> updateProcess(@RequestBody UpdateProcessRequest request) {
        return ResponseEntity.ok(processService.update(request));
    }
}
