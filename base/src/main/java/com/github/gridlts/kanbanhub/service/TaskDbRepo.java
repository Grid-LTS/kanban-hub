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

import static com.github.gridlts.kanbanhub.helper.DateUtilities.DATE_PATTERN;
import static java.time.ZoneOffset.UTC;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class TaskDbRepo {

    public static final String TIME_RANGE_ALL = "all";
    public static final String TIME_RANGE_RECENT = "recent";

    private TaskRepository taskRepository;
    private LastUpdatedRepository lastUpdatedRepository;



    private Map<TaskResourceType, ITaskResourceRepo> repos;

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
        for (Map.Entry<TaskResourceType, ITaskResourceRepo> source : repos.entrySet()) {
            source.getValue().initConsole();
            this.saveAllRecentTasksForType(source.getKey());
        }
    }

    public void saveAllTasksAuthentified(TaskResourceType resourceType, String range, String accessToken) {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        resourceRepo.init(accessToken);
        if (TIME_RANGE_RECENT.equals(range)) {
            saveAllRecentTasksForType(resourceType);
        }
        if (TIME_RANGE_ALL.equals(range)) {
            syncAllTasksInitial(resourceType);
        }
    }

    public void saveAllRecentTasksForType(TaskResourceType resourceType) {
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

    public boolean addRecentTasksForType(TaskResourceType resourceType, ZonedDateTime lastSavedTime) {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllTasksNewerThan(lastSavedTime);
        persistTasks(recentTasks, resourceType, new ArrayList<>());
        return recentTasks.size() > 0;
    }

    public Boolean syncAllTasksInitial(TaskResourceType resourceType) {
        ZonedDateTime startDateTime = DateUtilities.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllTasksNewerThan(startDateTime);
        List<TaskEntity> leftOver = new ArrayList<>();
        persistTasks(initialTaskList, resourceType, leftOver);
        for (TaskEntity taskEntity : leftOver) {
            if (TaskStatus.COMPLETED == taskEntity.getStatus()) {
                continue;
            }
            deleteTask(taskEntity);
        }
        deleteTasksDeletedUpstream(resourceType, startDateTime);
        return initialTaskList.size() > 0;
    }

    public void deleteTasksDeletedUpstream(TaskResourceType resourceType, ZonedDateTime startDateTime) {
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> deletedTasks = resourceRepo.getDeletedTasks(startDateTime);
        for (BaseTaskDto deletedTask : deletedTasks) {
            Optional<TaskEntity> deletedTaskEntityOptional = taskRepository
                    .findDistinctByResourceAndResourceId(resourceType.toString(),
                    deletedTask.getTaskId());
            deletedTaskEntityOptional.ifPresent(this::deleteTask);
        }
    }

    public List<BaseTaskDto> getAllTasksCompletedAfter(TaskResourceType resourceType, Instant lowerTimeLimit) {
        return getAllTasksUpdatedAfter(resourceType, TaskStatus.COMPLETED, lowerTimeLimit)
                .stream()
                .filter(taskEntity ->
                        !taskEntity.getCompletionDate().isBefore(lowerTimeLimit.atZone(UTC).toLocalDate()))
                .map(this::convertTaskToNewModel)
                .collect(Collectors.toList());
    }

    public Map<TaskResourceType, ITaskResourceRepo> getRepos() {
        return repos;
    }

    private List<TaskEntity> getAllTasksUpdatedAfter(TaskResourceType resourceType,
                                                    TaskStatus status, Instant lowerTimeLimit) {
        return taskRepository.findAllByResourceAndStatusAndUpdateTimeAfter(
                resourceType.toString(), status, lowerTimeLimit);
    }

    private ITaskResourceRepo getRepoForResourceType(TaskResourceType resourceType) {
        ITaskResourceRepo resourceRepo = repos.get(resourceType);
        if (resourceRepo == null) {
            throw new ResourceNotFoundException(resourceType.toString());
        }
        return resourceRepo;
    }

    private Optional<ZonedDateTime> getLastUpdatedTime(TaskResourceType resourceType) {
        return lastUpdatedRepository.findOneByResource(resourceType.toString()).map
                (lastUpdated -> lastUpdated.getLastUpdated().atZone(ZoneId.of("UTC")));
    }

    private void updateTimestamp(TaskResourceType resourceType) {
        LastUpdatedEntity lastUpdatedTimestamp = lastUpdatedRepository
                .findOneByResource(resourceType.toString()).orElse(new LastUpdatedEntity());
        lastUpdatedTimestamp.setResource(resourceType.toString());
        lastUpdatedTimestamp.setLastUpdated(LocalDateTime.now(ZoneId.of("UTC")));
        lastUpdatedRepository.save(lastUpdatedTimestamp);
    }

    private void persistTasks(List<BaseTaskDto> tasks, TaskResourceType resourceType, List<TaskEntity> leftOver) {
        List<TaskEntity> existingTaskEntities = taskRepository.findAllByResource(resourceType.toString());
        Map<String, TaskEntity> existingTaskEntitiesByTitleAndDate = new HashMap<>();
        // use creation date + task title as identifier because some task managers do not define a
        // native id for their tasks
        existingTaskEntities.forEach(task -> existingTaskEntitiesByTitleAndDate
                .put(getTaskIdentifier(task), task)
        );
        List<TaskEntity> taskEntities = tasks.stream()
                .map(taskDto -> {
                    String identifier = getTaskIdentifier(taskDto);
                    if (existingTaskEntitiesByTitleAndDate.containsKey(identifier)) {
                        TaskEntity taskEntity = existingTaskEntitiesByTitleAndDate.get(identifier);
                        existingTaskEntitiesByTitleAndDate.remove(identifier);
                        if (!isTaskEntityToBeUpdated(taskEntity, taskDto)) {
                            return null;
                        }
                        return this.updateTaskEntityWithTaskDto(taskDto, taskEntity);
                    } else {
                        return this.convertTaskToNewModel(taskDto);
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        leftOver.addAll(existingTaskEntitiesByTitleAndDate.values());
        taskRepository.saveAll(taskEntities);
        for (TaskEntity task : taskEntities) {
            log.info("Saved task {}, tagged {}, resource {}", task.getTitle(), task.getTags(), resourceType);
        }
    }

    private String getTaskIdentifier(TaskEntity task) {
        if (task.getResourceId() != null && !task.getResourceId().isEmpty()) {
            return task.getResourceId();
        }
        return task.getCreationDate() + "|" + task.getTitle();
    }

    private String getTaskIdentifier(BaseTaskDto task) {
        if (task.getTaskId() != null && !task.getTaskId().isEmpty()) {
            return task.getTaskId();
        }
        return task.getCreationDate().format(DATE_PATTERN) + "|" + task.getTitle();
    }

    private void deleteTask(TaskEntity taskEntity) {
        log.info("Delete task: resource={}, title={}, tags={}, projectCode={}, description={}, created={}.",
                taskEntity.getResource(), taskEntity.getTitle(), taskEntity.getTags(),
                taskEntity.getProjectCode(), taskEntity.getDescription(),
                taskEntity.getCreationDate());
        taskRepository.delete(taskEntity);
    }

    private TaskEntity convertTaskToNewModel(BaseTaskDto task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskId(UUID.randomUUID().toString());
        taskEntity.setCreationDate(task.getCreationDate());
        taskEntity.setResource(task.getResource().toString());
        return updateTaskEntityWithTaskDto(task, taskEntity);
    }

    private boolean isTaskEntityToBeUpdated(TaskEntity taskEntity, BaseTaskDto task) {
        return !taskEntity.getTitle().equals(task.getTitle())
                || !TaskResourceType.getResourceType(taskEntity.getResource()).equals(task.getResource())
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
            taskEntity.setCompletionDate(task.getCompleted());
        }
        taskEntity.setResource(task.getResource().toString());
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
                .creationDate(taskEntity.getCreationDate())
                .completed(taskEntity.getCompletionDate() != null ? taskEntity.getCompletionDate() : null)
                .description(taskEntity.getDescription())
                .status(taskEntity.getStatus())
                .projectCode(taskEntity.getProjectCode())
                .resource(TaskResourceType.getResourceType(taskEntity.getResource()))
                .addAllTags(taskEntity.getTags())
                .projectCode(taskEntity.getProjectCode())
                .build();
    }

}
