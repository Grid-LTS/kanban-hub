package com.github.gridlts.kanbanhub.service;

import com.github.gridlts.kanbanhub.model.LastUpdatedEntity;
import com.github.gridlts.kanbanhub.model.TaskEntity;
import com.github.gridlts.kanbanhub.repository.LastUpdatedRepository;
import com.github.gridlts.kanbanhub.repository.TaskRepository;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.TaskResourceType;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class TaskDbRepo {

    private TaskRepository taskRepository;
    private LastUpdatedRepository lastUpdatedRepository;
    private Map<String,ITaskResourceRepo> repos;

    @Autowired
    public TaskDbRepo(List<ITaskResourceRepo> repos, TaskRepository taskRepository,
               LastUpdatedRepository lastUpdatedRepository) {
        this.repos = new HashedMap<>();
        for (ITaskResourceRepo repo: repos) {
            this.repos.put(repo.getResourceType(), repo);
        }
        this.taskRepository = taskRepository;
        this.lastUpdatedRepository = lastUpdatedRepository;
    }

    public void saveAllCompletedTasksConsole() throws IOException {
        for (Map.Entry<String, ITaskResourceRepo> source : repos.entrySet()) {
            source.getValue().initConsole();
            this.saveAllCompletedTasksForType(source.getKey());
        }
    }

    public void saveAllCompletedTasksForType(String resourceType)
            throws IOException {
        boolean tasksSaved;
        Optional<ZonedDateTime> lastUpdatedTime = getLastUpdatedTime(resourceType);
        if (lastUpdatedTime.isEmpty()) {
            tasksSaved = saveAllTasksInitial(resourceType);
        } else {
            tasksSaved = addRecentCompletedTasksForType(resourceType, lastUpdatedTime.get());
        }
        if (!tasksSaved) {
            log.info(String.format("No tasks could be found for resource %s.", resourceType));
        }
        updateTimestamp(resourceType);
    }

    public boolean addRecentCompletedTasksForType(String resourceType, ZonedDateTime lastSavedTime)
            throws IOException {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllCompletedTasksNewerThan(lastSavedTime);
        persistTasks(recentTasks);
        return recentTasks.size() > 0;
    }

    public ITaskResourceRepo getRepoForResourceType(String resourceType) {
        ITaskResourceRepo resourceRepo = repos.get(resourceType);
        if (resourceRepo == null)  {
            throw new RuntimeException("Unknown Resource type");
        }
        return resourceRepo;
    }

    public Optional<ZonedDateTime> getLastUpdatedTime(String resourceType) {
        return lastUpdatedRepository.findOneByResource(resourceType).map
                (lastUpdated -> lastUpdated.getLastUpdated().atZone(ZoneId.of("UTC")));
    }

    public void updateTimestamp(String resourceType){
        LastUpdatedEntity lastUpdatedTimestamp = lastUpdatedRepository
                .findOneByResource(resourceType).orElse(new LastUpdatedEntity());
        lastUpdatedTimestamp.setResource(resourceType);
        lastUpdatedTimestamp.setLastUpdated(LocalDateTime.now(ZoneId.of("UTC")));
        lastUpdatedRepository.save(lastUpdatedTimestamp);
    }

    public Boolean saveAllTasksInitial(String resourceType) throws IOException {
        ZonedDateTime startDateTime = DateTimeHelper.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllCompletedTasksNewerThan(startDateTime);
        persistTasks(initialTaskList);
        return initialTaskList.size() > 0;
    }

    public void persistTasks(List<BaseTaskDto> tasks) {
        List<TaskEntity> taskEntities = tasks.stream().map(this::convertTaskToNewModel).collect(Collectors.toList());
        taskRepository.saveAll(taskEntities);
        for (BaseTaskDto task : tasks) {
            log.info("Saved task {}, tagged {}", task.getTitle(), task.getTags());
        }
    }

    public List<BaseTaskDto> getAllTasksInsertedBefore(TaskResourceType resourceType, Instant lowerTimeLimit) {
        List<TaskEntity> taskEntities = taskRepository.findAllByResourceAndInsertTimeAfter(
                resourceType.toString(), lowerTimeLimit);
        return taskEntities.stream().map(this::convertTaskToNewModel).collect(Collectors.toList());
    }

    private TaskEntity convertTaskToNewModel(BaseTaskDto task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTitle(task.getTitle());
        taskEntity.setTaskId(UUID.randomUUID().toString());
        taskEntity.setResourceId(task.getTaskId());
        taskEntity.setResource(task.getSource().toString());
        taskEntity.setCompletionDate(task.getCompleted().atTime(0,0)
                .toInstant(ZoneOffset.UTC));
        taskEntity.setDescription(task.getDescription());
        taskEntity.setProjectCode(task.getProjectCode());
        taskEntity.setTags(task.getTags());
        return taskEntity;
    }

    private BaseTaskDto convertTaskToNewModel(TaskEntity taskEntity) {
        return new BaseTaskDto.Builder()
                .taskId(taskEntity.getResourceId())
                .title(taskEntity.getTitle())
                .completed(LocalDate.ofInstant(taskEntity.getCompletionDate(), ZoneOffset.UTC))
                .description(taskEntity.getDescription())
                .projectCode(taskEntity.getProjectCode())
                .source(TaskResourceType.getResourceType(taskEntity.getResource()))
                .addAllTags(taskEntity.getTags())
                .projectCode(taskEntity.getProjectCode())
                .build();
    }

}
