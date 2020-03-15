package com.github.gridlts.khapi.service;

import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.github.gridlts.khapi.resources.ITaskResourceRepo;
import com.github.gridlts.khapi.resources.TaskResourceType;
import com.github.gridlts.khapi.taskw.service.TaskwRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TaskDbRepo {

    private GTaskRepo gTaskRepo;
    private TaskwRepo taskwRepo;

    @Autowired
    TaskDbRepo(GTaskRepo gTaskRepo, TaskwRepo taskwRepo) {
        this.gTaskRepo = gTaskRepo;
        this.taskwRepo = taskwRepo;
    }

    public void saveAllCompletedTasks(String accessToken) throws IOException, GeneralSecurityException {
        // initialize sessions of resources
        this.gTaskRepo.instantiateGapiService(accessToken);
        this.saveAllCompletedTasksForType(TaskResourceType.GOOGLE_TASKS);
        this.saveAllCompletedTasksForType(TaskResourceType.TASKWARRIOR);
    }

    public void saveAllCompletedTasksForType(TaskResourceType resourceType) throws IOException {

    }

    public boolean addRecentCompletedTasksForType(TaskResourceType resourceType, ZonedDateTime lastSavedTime)
            throws IOException {

        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllCompletedTasksNewerThan(lastSavedTime);
        // todo save in database
        return recentTasks.size() > 0;
    }

    public ITaskResourceRepo getRepoForResourceType(TaskResourceType resourceType) {
        switch (resourceType) {
            case TASKWARRIOR:
                return this.taskwRepo;
            case GOOGLE_TASKS:
                return this.gTaskRepo;
            default:
                throw new RuntimeException("Unknown Resource type");
        }
    }

    public ZonedDateTime getLastUpdatedTime(String propertyName) throws IOException {
        // todo from database
        String unixTimeString = Instant.now().toString();
        long unixTime = Long.parseLong(unixTimeString, 10);
        return DateTimeHelper.convertUnixTimestampToZonedDateTime(unixTime);
    }

    public Boolean saveAllTasksInitial(TaskResourceType resourceType) throws IOException {
        ZonedDateTime startDateTime = DateTimeHelper.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllCompletedTasksNewerThan(startDateTime);
        // todo database call
        return initialTaskList.size() > 0;
    }
}
