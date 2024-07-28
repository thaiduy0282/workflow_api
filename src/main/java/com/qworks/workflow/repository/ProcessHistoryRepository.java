package com.qworks.workflow.repository;

import com.qworks.workflow.entity.ProcessHistoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessHistoryRepository extends MongoRepository<ProcessHistoryEntity, String> {

    Optional<ProcessHistoryEntity> findProcessHistoryEntityByActivityId(String activityId);

}
