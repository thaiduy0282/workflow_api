package com.qworks.workflow.service;

import com.qworks.workflow.dto.ProcessHistoryDto;
import com.qworks.workflow.dto.request.CreateProcessHistoryRequest;
import com.qworks.workflow.dto.request.UpdateProcessHistoryRequest;

import java.util.List;

public interface ProcessHistoryService {

    ProcessHistoryDto findByActivityIdId(String activityId);

    ProcessHistoryDto create(CreateProcessHistoryRequest request);

    ProcessHistoryDto update(String activityId, UpdateProcessHistoryRequest request);

    void saveAll(List<ProcessHistoryDto> processHistoryLst);

}
