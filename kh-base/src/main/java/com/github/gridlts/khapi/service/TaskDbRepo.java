package com.github.gridlts.khapi.service;

import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.github.gridlts.khapi.model.LastUpdatedEntity;
import com.github.gridlts.khapi.model.TaskEntity;
import com.github.gridlts.khapi.repository.LastUpdatedRepository;
import com.github.gridlts.khapi.repository.TaskRepository;
import com.github.gridlts.khapi.resources.ITaskResourceRepo;
import com.github.gridlts.khapi.resources.TaskResourceType;
import com.github.gridlts.khapi.taskw.service.TaskwRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskDbRepo {

    private GTaskRepo gTaskRepo;
    private TaskwRepo taskwRepo;
    private TaskRepository taskRepository;
    private LastUpdatedRepository lastUpdatedRepository;

    @Autowired
    TaskDbRepo(GTaskRepo gTaskRepo, TaskwRepo taskwRepo, TaskRepository taskRepository,
               LastUpdatedRepository lastUpdatedRepository) {
        this.gTaskRepo = gTaskRepo;
        this.taskwRepo = taskwRepo;
        this.taskRepository = taskRepository;
        this.lastUpdatedRepository = lastUpdatedRepository;
    }

    public void saveAllCompletedTasks(String accessToken) throws IOException, GeneralSecurityException {
        // initialize sessions of resources
        this.gTaskRepo.instantiateGapiService(accessToken);
        this.saveAllCompletedTasksForType(TaskResourceType.GOOGLE_TASKS);
        this.saveAllCompletedTasksForType(TaskResourceType.TASKWARRIOR);
    }

    public void saveAllCompletedTasksForType(TaskResourceType resourceType)
            throws IOException {
        boolean tasksSaved;
        Optional<ZonedDateTime> lastUpdatedTime = getLastUpdatedTime(resourceType);
        if (lastUpdatedTime.isEmpty()) {
            tasksSaved = saveAllTasksInitial(resourceType);
        } else {
            tasksSaved = addRecentCompletedTasksForType(resourceType, lastUpdatedTime.get());
        }
        if (!tasksSaved) {
            log.warn(String.format("No tasks could be found for resource %s.", resourceType));
        }
        updateTimestamp(resourceType);
    }

    public boolean addRecentCompletedTasksForType(TaskResourceType resourceType, ZonedDateTime lastSavedTime)
            throws IOException {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllCompletedTasksNewerThan(lastSavedTime);
        persistTasks(recentTasks);
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

    public Optional<ZonedDateTime> getLastUpdatedTime(TaskResourceType resourceType) {
        return lastUpdatedRepository.findOneByResource(resourceType.toString()).map
                (lastUpdated -> lastUpdated.getLastUpdated().atZone(ZoneId.of("UTC")));
    }

    public void updateTimestamp(TaskResourceType resourceType){
        LastUpdatedEntity lastUpdatedTimestamp = lastUpdatedRepository
                .findOneByResource(resourceType.toString()).orElse(new LastUpdatedEntity());
        lastUpdatedTimestamp.setResource(resourceType.toString());
        lastUpdatedTimestamp.setLastUpdated(LocalDateTime.now(ZoneId.of("UTC")));
        lastUpdatedRepository.save(lastUpdatedTimestamp);
    }

    public Boolean saveAllTasksInitial(TaskResourceType resourceType) throws IOException {
        ZonedDateTime startDateTime = DateTimeHelper.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllCompletedTasksNewerThan(startDateTime);
        persistTasks(initialTaskList);
        return initialTaskList.size() > 0;
    }

    public void persistTasks(List<BaseTaskDto> tasks) {
        List<TaskEntity> taskEntities = tasks.stream().map(this::convertTaskToNewModel).collect(Collectors.toList());
        taskRepository.saveAll(taskEntities);
    }

    private TaskEntity convertTaskToNewModel(BaseTaskDto task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTitle(task.getTitle());
        taskEntity.setTaskId(UUID.randomUUID().toString());
        taskEntity.setResourceId(task.getTaskId());
        taskEntity.setResource(task.getSource().toString());
        taskEntity.setCompletionDate(task.getCompleted());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setProjectCode(task.getProjectCode());
        taskEntity.setTags(task.getTags());
        return taskEntity;
    }

}
