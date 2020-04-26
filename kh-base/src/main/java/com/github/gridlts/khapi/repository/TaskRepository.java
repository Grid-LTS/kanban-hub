package com.github.gridlts.khapi.repository;

import com.github.gridlts.khapi.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, String> {

    @Query("SELECT max(task.completionDate) FROM TaskEntity task")
    LocalDate getMaxCompletionDate();

    List<TaskEntity> findAllByResourceAndInsertTimeAfter(String resourceType,
                                                         Instant lowerDateLimit);
}
