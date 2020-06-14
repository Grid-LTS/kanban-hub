package com.github.gridlts.kanbanhub.repository;

import com.github.gridlts.kanbanhub.model.TaskEntity;
import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, String> {

    @Query("SELECT max(task.completionDate) FROM TaskEntity task")
    LocalDate getMaxCompletionDate();

    List<TaskEntity> findAllByResource(String resourceType);

    List<TaskEntity> findAllByResourceAndStatusAndInsertTimeAfter(String resourceType, TaskStatus status,
                                                                  Instant lowerDateLimit);
    List<TaskEntity> findAllByResourceAndInsertTimeAfter(String resourceType,
                                                                  Instant lowerDateLimit);
}
