package com.github.gridlts.kanbanhub.gtasks;

import com.github.gridlts.kanbanhub.gtasks.service.GTasksApiService;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceConfiguration;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import com.github.gridlts.kanbanhub.sources.api.dto.TaskListDto;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.gridlts.kanbanhub.gtasks.DateTimeHelper.convertZoneDateTimeToRFC3339Timestamp;
import static com.github.gridlts.kanbanhub.sources.api.TaskResourceType.GOOGLE_TASKS;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GTaskRepo implements ITaskResourceRepo {

    private static final Long MAX_RESULTS = 10000L;

    private Tasks tasksService;
    private final GTasksApiService gTasksApiService;
    private final GTasksConfiguration gTasksConfiguration;

    @Override
    public void init(String accessToken) {
        // initialize sessions of resources
        try {
            this.tasksService = this.gTasksApiService.instantiateGapiService(accessToken);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initConsole() {
        this.tasksService = this.gTasksApiService.instantiateGapiServiceConsole();
    }

    @Override
    public String getResourceType() {
        return "google_tasks";
    }

    @Override
    public ITaskResourceConfiguration getResourceConfiguration() {
        return gTasksConfiguration;
    }

    @Override
    public List<TaskListDto> getTaskListsEntry(String accessToken) {
        try {
            this.tasksService = gTasksApiService.instantiateGapiService(accessToken);
            return this.getTaskLists().stream().map(this::mapTaskListToDto).collect(Collectors.toList());
        } catch (IOException io) {

        } catch (GeneralSecurityException generalSecurityException) {

        }
        return new ArrayList<>();
    }

    public List<TaskList> getTaskLists() {
        List<TaskList> taskLists = new ArrayList<>();
        try {
            TaskLists result = this.tasksService.tasklists().list()
                    .setMaxResults(20L)
                    .execute();
            taskLists = result.getItems();
        } catch (IOException ex) {
            return taskLists;
        }
        if (taskLists == null) {
            taskLists = new ArrayList<>();
        }
        return taskLists;
    }

    @Override
    public List<BaseTaskDto> getOpenTasksForTaskListEntry(String taskListId, String accessToken) {
        try {
            this.tasksService = gTasksApiService.instantiateGapiService(accessToken);
            return this.getOpenTasksForTaskList(taskListId).stream()
                    .map(task -> mapTaskToDto(task, taskListId))
                    .collect(Collectors.toList());
        } catch (IOException io) {

        } catch (GeneralSecurityException generalSecurityException) {

        }
        return new ArrayList<>();
    }

    public List<Task> getOpenTasksForTaskList(String taskListId)
            throws IOException {
        com.google.api.services.tasks.model.Tasks result = this.tasksService.tasks().list(taskListId)
                .setMaxResults(MAX_RESULTS)
                .setShowCompleted(false)
                .execute();
        List<Task> tasksForTaskList = result.getItems();
        if (tasksForTaskList == null) {
            tasksForTaskList = new ArrayList<>();
        }
        return tasksForTaskList;
    }


    public List<Task> getCompletedTasksForTaskList(String taskListId, ZonedDateTime newerThanDateTime) {
        List<Task> tasksForTaskList = new ArrayList<>();
        try {
            com.google.api.services.tasks.model.Tasks result = this.tasksService.tasks().list(taskListId)
                    .setMaxResults(MAX_RESULTS)
                    .setCompletedMin(convertZoneDateTimeToRFC3339Timestamp(newerThanDateTime))
                    .setShowCompleted(true)
                    .setShowHidden(true)
                    .execute();
            tasksForTaskList = result.getItems();
        } catch (IOException exception) {
            return tasksForTaskList;
        }
        if (tasksForTaskList == null) {
            tasksForTaskList = new ArrayList<>();
        }
        return tasksForTaskList;
    }

    public List<Task> getTasksForTaskList(String taskListId, ZonedDateTime newerThanDateTime,
                                          boolean showCompleted, boolean showDeleted) {
        List<Task> tasksForTaskList = new ArrayList<>();
        try {
            com.google.api.services.tasks.model.Tasks result = this.tasksService.tasks().list(taskListId)
                    .setMaxResults(MAX_RESULTS)
                    .setUpdatedMin(convertZoneDateTimeToRFC3339Timestamp(newerThanDateTime))
                    .setShowCompleted(showCompleted)
                    .setShowDeleted(showDeleted)
                    .setShowHidden(true)
                    .execute();
            tasksForTaskList = result.getItems();
        } catch (IOException exception) {
            return tasksForTaskList;
        }
        if (tasksForTaskList == null) {
            tasksForTaskList = new ArrayList<>();
        }
        return tasksForTaskList;
    }

    public List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime completedAfterDateTime) {
        List<TaskList> taskLists = this.getTaskLists();
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        for (TaskList taskList : taskLists) {
            List<Task> tasks = this.getCompletedTasksForTaskList(taskList.getId(), completedAfterDateTime);
            for (Task task : tasks) {
                // consistency checks before conversion for saving to file
                if (task.getCompleted() == null) {
                    continue;
                }
                ZonedDateTime taskDateTime = DateTimeHelper.convertGoogleTimeToZonedDateTime(task.getCompleted());
                if (taskDateTime.isBefore(completedAfterDateTime)) {
                    continue;
                }
                BaseTaskDto baseTaskDto = mapTaskToDto(task, taskList.getId());
                convertedTaskList.add(baseTaskDto);
            }
        }
        return convertedTaskList;
    }

    @Override
    public List<BaseTaskDto> getAllTasksNewerThan(ZonedDateTime newerThanDateTime) {
        List<TaskList> taskLists = this.getTaskLists();
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        for (TaskList taskList : taskLists) {
            List<Task> tasks = this.getTasksForTaskList(taskList.getId(), newerThanDateTime,
                    true, false);
            for (Task task : tasks) {
                // consistency checks before conversion for saving to file
                ZonedDateTime taskDateTime = DateTimeHelper.convertGoogleTimeToZonedDateTime(task.getUpdated());
                if (taskDateTime.isBefore(newerThanDateTime)) {
                    continue;
                }
                BaseTaskDto baseTaskDto = mapTaskToDto(task, taskList.getId());
                convertedTaskList.add(baseTaskDto);
            }
        }
        return convertedTaskList;
    }

    @Override
    public List<BaseTaskDto> getDeletedTasks(ZonedDateTime newerThanDateTime) {
        List<TaskList> taskLists = this.getTaskLists();
        List<BaseTaskDto> deletedList = new ArrayList<>();
        for (TaskList taskList : taskLists) {
            List<Task> tasksForTaskList = this.getTasksForTaskList(taskList.getId(),
                    newerThanDateTime, false, true);
            for (Task task : tasksForTaskList) {
                if (task.getDeleted() == null || !task.getDeleted()) {
                    continue;
                }
                BaseTaskDto baseTaskDto = mapTaskToDto(task, taskList.getId());
                deletedList.add(baseTaskDto);
            }
        }
        return deletedList;
    }



    private TaskListDto mapTaskListToDto(TaskList taskList) {
        return new TaskListDto.Builder()
                .id(taskList.getId())
                .title(taskList.getTitle())
                .build();
    }

    private BaseTaskDto mapTaskToDto(Task task, String taskListId) {
        DateTime dateUpdated;
        if (task.getCompleted() != null &&
                task.getUpdated().getValue() > task.getCompleted().getValue()) {
            dateUpdated = task.getCompleted();
        } else {
            dateUpdated = task.getUpdated();
        }
        LocalDate completedDate;
        TaskStatus status;
        if (task.getCompleted() == null) {
            completedDate = null;
            status = TaskStatus.PENDING;
        } else {
            completedDate = DateTimeHelper.convertGoogleTimeToDate(task.getCompleted());
            status = TaskStatus.COMPLETED;
        }
        return new BaseTaskDto.Builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .description(task.getNotes())
                .status(status)
                .creationDate(DateTimeHelper.convertGoogleTimeToDate(dateUpdated))
                .completed(completedDate)
                .source(GOOGLE_TASKS)
                .taskListId(taskListId)
                .build();
    }
}
