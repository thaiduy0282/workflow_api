package com.qworks.workflow.controller;

import com.qworks.workflow.dto.ProcessHistoryDto;
import com.qworks.workflow.dto.request.CreateProcessHistoryRequest;
import com.qworks.workflow.dto.request.UpdateProcessHistoryRequest;
import com.qworks.workflow.service.ProcessHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/processHistory")
@RequiredArgsConstructor
@Slf4j
public class ProcessHistoryController {

    private final ProcessHistoryService processHistoryService;

    @PostMapping
    public ResponseEntity<ProcessHistoryDto> createProcessHistory(@RequestBody CreateProcessHistoryRequest request) {
        return ResponseEntity.ok(processHistoryService.create(request));
    }

    @PatchMapping
    public ResponseEntity<ProcessHistoryDto> updateProcessHistory(@RequestBody UpdateProcessHistoryRequest request, @RequestParam(name = "activityId") String activityId) {
        return ResponseEntity.ok(processHistoryService.update(activityId, request));
    }

}
