package com.github.gridlts.kanbanhub.service;

import com.github.gridlts.kanbanhub.exception.ResourceNotFoundException;
import com.github.gridlts.kanbanhub.helper.DateUtilities;
import com.github.gridlts.kanbanhub.model.LastUpdatedEntity;
import com.github.gridlts.kanbanhub.model.TaskEntity;
import com.github.gridlts.kanbanhub.repository.LastUpdatedRepository;
import com.github.gridlts.kanbanhub.repository.TaskRepository;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.TaskResourceType;
import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class TaskDbRepo {

    public static final String TIME_RANGE_ALL = "all";
    public static final String TIME_RANGE_RECENT = "recent";

    private TaskRepository taskRepository;
    private LastUpdatedRepository lastUpdatedRepository;
    private Map<String, ITaskResourceRepo> repos;

    @Autowired
    public TaskDbRepo(List<ITaskResourceRepo> repos, TaskRepository taskRepository,
                      LastUpdatedRepository lastUpdatedRepository) {
        this.repos = new HashMap<>();
        for (ITaskResourceRepo repo : repos) {
            this.repos.put(repo.getResourceType(), repo);
        }
        this.taskRepository = taskRepository;
        this.lastUpdatedRepository = lastUpdatedRepository;
    }

    public void saveAllRecentTasksConsole() {
        for (Map.Entry<String, ITaskResourceRepo> source : repos.entrySet()) {
            source.getValue().initConsole();
            this.saveAllRecentTasksForType(source.getKey());
        }
    }

    public void saveAllTasksAuthentified(String resourceType, String range, String accessToken) {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        resourceRepo.init(accessToken);
        if (TIME_RANGE_RECENT.equals(range)) {
            saveAllRecentTasksForType(resourceType);
        }
        if (TIME_RANGE_ALL.equals(range)) {
            syncAllTasksInitial(resourceType);
        }
    }

    public void saveAllRecentTasksForType(String resourceType) {
        boolean tasksSaved;
        Optional<ZonedDateTime> lastUpdatedTime = getLastUpdatedTime(resourceType);
        if (lastUpdatedTime.isEmpty()) {
            tasksSaved = syncAllTasksInitial(resourceType);
        } else {
            tasksSaved = addRecentTasksForType(resourceType, lastUpdatedTime.get());
        }
        if (!tasksSaved) {
            log.info(String.format("No tasks could be found for resource %s.", resourceType));
        }
        updateTimestamp(resourceType);
    }

    public boolean addRecentTasksForType(String resourceType, ZonedDateTime lastSavedTime) {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllTasksNewerThan(lastSavedTime);
        persistTasks(recentTasks, resourceType);
        return recentTasks.size() > 0;
    }

    private ITaskResourceRepo getRepoForResourceType(String resourceType) {
        ITaskResourceRepo resourceRepo = repos.get(resourceType);
        if (resourceRepo == null) {
            throw new ResourceNotFoundException(resourceType);
        }
        return resourceRepo;
    }

    public Optional<ZonedDateTime> getLastUpdatedTime(String resourceType) {
        return lastUpdatedRepository.findOneByResource(resourceType).map
                (lastUpdated -> lastUpdated.getLastUpdated().atZone(ZoneId.of("UTC")));
    }

    public void updateTimestamp(String resourceType) {
        LastUpdatedEntity lastUpdatedTimestamp = lastUpdatedRepository
                .findOneByResource(resourceType).orElse(new LastUpdatedEntity());
        lastUpdatedTimestamp.setResource(resourceType);
        lastUpdatedTimestamp.setLastUpdated(LocalDateTime.now(ZoneId.of("UTC")));
        lastUpdatedRepository.save(lastUpdatedTimestamp);
    }

    public Boolean syncAllTasksInitial(String resourceType) {
        ZonedDateTime startDateTime = DateUtilities.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllTasksNewerThan(startDateTime);
        persistTasks(initialTaskList, resourceType);
        deleteTasks(resourceType, startDateTime);
        return initialTaskList.size() > 0;
    }

    public void persistTasks(List<BaseTaskDto> tasks, String resourceType) {
        List<TaskEntity> existingTaskEntities = taskRepository.findAllByResource(resourceType);
        Map<String, TaskEntity> existingTaskEntitiesByNativeId = new HashMap<>();
        existingTaskEntities.forEach(task -> existingTaskEntitiesByNativeId.put(task.getResourceId(), task));
        List<TaskEntity> taskEntities = tasks.stream()
                .map(taskDto -> {
                    if (existingTaskEntitiesByNativeId.containsKey(taskDto.getTaskId())) {
                        TaskEntity taskEntity = existingTaskEntitiesByNativeId.get(taskDto.getTaskId());
                        if (!isTaskEntityToBeUpdated(taskEntity, taskDto)) {
                            return null;
                        }
                        return this.updateTaskEntityWithTaskDto(taskDto, taskEntity);
                    } else {
                        return this.convertTaskToNewModel(taskDto);
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        taskRepository.saveAll(taskEntities);
        for (TaskEntity task : taskEntities) {
            log.info("Saved task {}, tagged {}", task.getTitle(), task.getTags());
        }
    }

    public void deleteTasks(String resourceType, ZonedDateTime startDateTime) {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> deletedTasks = resourceRepo.getDeletedTasks(startDateTime);
        for (BaseTaskDto deletedTask : deletedTasks) {
            Optional<TaskEntity> deletedTaskEntityOptional = taskRepository.findDistinctByResourceAndResourceId(resourceType,
                    deletedTask.getTaskId());
            deletedTaskEntityOptional.ifPresent(taskEntity -> {
                log.info("Delete task: resource={}, title={}, list={}, description={}, created={}.",
                        taskEntity.getResource(), taskEntity.getTitle(), taskEntity.getProjectCode(),
                        taskEntity.getDescription(), taskEntity.getCreationDate());
                taskRepository.delete(taskEntity);
            });
        }
    }


    public List<BaseTaskDto> getAllTasksUpdatedAfter(TaskResourceType resourceType, Instant lowerTimeLimit) {
        List<TaskEntity> taskEntities = taskRepository.findAllByResourceAndUpdateTimeAfter(
                resourceType.toString(), lowerTimeLimit);
        return taskEntities.stream().map(this::convertTaskToNewModel).collect(Collectors.toList());
    }

    public List<BaseTaskDto> getAllTasksUpdatedAfter(TaskResourceType resourceType,
                                                     TaskStatus status, Instant lowerTimeLimit) {
        List<TaskEntity> taskEntities = taskRepository.findAllByResourceAndStatusAndUpdateTimeAfter(
                resourceType.toString(), status, lowerTimeLimit);
        return taskEntities.stream().map(this::convertTaskToNewModel).collect(Collectors.toList());
    }

    private TaskEntity convertTaskToNewModel(BaseTaskDto task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskId(UUID.randomUUID().toString());
        taskEntity.setCreationDate(task.getCreationDate().atTime(0, 0)
                .toInstant(ZoneOffset.UTC));
        taskEntity.setResource(task.getSource().toString());
        return updateTaskEntityWithTaskDto(task, taskEntity);
    }

    private boolean isTaskEntityToBeUpdated(TaskEntity taskEntity, BaseTaskDto task) {
        return !taskEntity.getTitle().equals(task.getTitle())
                || !taskEntity.getResourceId().equals(task.getTaskId())
                || (taskEntity.getCompletionDate() == null && task.getCompleted() != null)
                || (taskEntity.getDescription() != null
                && !taskEntity.getDescription().equals(task.getDescription()))
                || (taskEntity.getProjectCode() != null && !taskEntity.getProjectCode().equals(task.getProjectCode()))
                || (taskEntity.getTags() != null && !taskEntity.getTags().equals(task.getTags()))
                || (!taskEntity.getStatus().equals(task.getStatus()));
    }

    private TaskEntity updateTaskEntityWithTaskDto(BaseTaskDto task, TaskEntity taskEntity) {
        taskEntity.setTitle(task.getTitle());
        taskEntity.setResourceId(task.getTaskId());
        if (task.getCompleted() != null) {
            taskEntity.setCompletionDate(task.getCompleted().atTime(0, 0)
                    .toInstant(ZoneOffset.UTC));
        }
        taskEntity.setDescription(task.getDescription());
        taskEntity.setProjectCode(task.getProjectCode());
        taskEntity.setTags(task.getTags());
        taskEntity.setStatus(task.getStatus());
        return taskEntity;
    }

    private BaseTaskDto convertTaskToNewModel(TaskEntity taskEntity) {
        return new BaseTaskDto.Builder()
                .taskId(taskEntity.getResourceId())
                .title(taskEntity.getTitle())
                .creationDate(LocalDate.ofInstant(taskEntity.getCreationDate(), ZoneOffset.UTC))
                .completed(taskEntity.getCompletionDate() != null ? LocalDate.ofInstant(taskEntity.getCompletionDate(),
                        ZoneOffset.UTC) : null)
                .description(taskEntity.getDescription())
                .status(taskEntity.getStatus())
                .projectCode(taskEntity.getProjectCode())
                .source(TaskResourceType.getResourceType(taskEntity.getResource()))
                .addAllTags(taskEntity.getTags())
                .projectCode(taskEntity.getProjectCode())
                .build();
    }

}
