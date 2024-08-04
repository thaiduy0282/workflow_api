//package com.qworks.workflow.service.impl;
//
//import com.qworks.workflow.dto.ProcessHistoryDto;
//import com.qworks.workflow.dto.request.CreateProcessHistoryRequest;
//import com.qworks.workflow.dto.request.UpdateProcessRequest;
//import com.qworks.workflow.entity.ProcessEntity;
//import com.qworks.workflow.entity.ProcessHistoryEntity;
//import com.qworks.workflow.enums.ProcessHistoryStatus;
//import com.qworks.workflow.enums.ProcessStatus;
//import com.qworks.workflow.exception.ResourceNotFoundException;
//import com.qworks.workflow.mapper.ProcessHistoryMapper;
//import com.qworks.workflow.repository.ProcessHistoryRepository;
//import com.qworks.workflow.repository.ProcessRepository;
//import com.qworks.workflow.service.ProcessHistoryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ProcessHistoryServiceImpl implements ProcessHistoryService {
//
//    private final ProcessRepository processRepository;
//
//    private final ProcessHistoryRepository processHistoryRepository;
//
//    private final ProcessHistoryMapper processHistoryMapper;
//
//    @Override
//    public ProcessHistoryDto findByActivityIdId(String activityId) {
//        return processHistoryMapper.toDto(
//                processHistoryRepository.
//                        findProcessHistoryEntityByActivityId(activityId)
//                        .orElseThrow(() -> new ResourceNotFoundException("Process history not found with activityId: " + activityId))
//        );
//    }
//
//    @Override
//    public ProcessHistoryDto create(CreateProcessHistoryRequest request) {
//        ProcessHistoryEntity processHistoryEntity = new ProcessHistoryEntity();
//        processHistoryEntity.setProcessDefinitionId(request.processDefinitionId());
//        processHistoryEntity.setActivityName(request.activityName());
//        processHistoryEntity.setActivityId(request.activityId());
//        processHistoryEntity.setStatus(ProcessHistoryStatus.CREATED);
//        return processHistoryMapper.toDto(processHistoryRepository.save(processHistoryEntity));
//    }
//
//    @Override
//    @Transactional
//    public ProcessHistoryDto update(String activityId, UpdateProcessRequest request) {
//        ProcessHistoryEntity processHistoryEntity = processHistoryMapper.toEntity(findByActivityIdId(activityId));
//        processHistoryEntity.setStatus(request.status());
//        request.startDate().ifPresent(processHistoryEntity::setStartDate);
//        request.endDate().ifPresent(processHistoryEntity::setEndDate);
//        request.note().ifPresent(processHistoryEntity::setDescription);
//        ProcessHistoryDto processHistoryDto = processHistoryMapper.toDto(processHistoryRepository.save(processHistoryEntity));
//
//        if ("End".equals(processHistoryDto.getActivityName()) && ProcessHistoryStatus.SUCCESS.equals(processHistoryDto.getStatus())) {
//            ProcessEntity processEntity = processRepository.findByProcessDefinitionId(processHistoryDto.getProcessDefinitionId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Process history not found with processDefinitionId: " + processHistoryDto.getProcessDefinitionId()));
//            processEntity.setStatus(ProcessStatus.COMPLETED);
//            processEntity.setEndDate(request.endDate().orElse(new Date()));
//            processRepository.save(processEntity);
//        }
//
//        return processHistoryDto;
//    }
//
//    public void saveAll(List<ProcessHistoryDto> processHistoryLst) {
//        processHistoryRepository.saveAll(
//                processHistoryLst.stream()
//                        .map(processHistoryMapper::toEntity)
//                        .toList()
//        );
//    }
//
//}
//
