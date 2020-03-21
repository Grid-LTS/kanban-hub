package com.github.gridlts.khapi.gtasks.service;

import com.github.gridlts.khapi.dto.BaseTaskDto;

import com.github.gridlts.khapi.resources.ITaskResourceRepo;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.github.gridlts.khapi.gtasks.service.DateTimeHelper.convertZoneDateTimeToRFC3339Timestamp;
import static com.github.gridlts.khapi.resources.TaskResourceType.GOOGLE_TASKS;

@Service
public class GTaskRepo implements ITaskResourceRepo {

    private static final Long MAX_RESULTS = 10000L;

    private Tasks tasksService;

    GTaskRepo(GTasksApiService gTasksApiService) {
    }

    public List<TaskList> getTaskListsEntry(String accessToken) throws IOException, GeneralSecurityException {
        this.tasksService = GTasksApiService.instantiateGapiService(accessToken);
        return this.getTaskLists();
    }

    public List<TaskList> getTaskLists() throws IOException {
        TaskLists result = this.tasksService.tasklists().list()
                .setMaxResults(10L)
                .execute();
        List<TaskList> taskLists = result.getItems();
        if (taskLists == null) {
            taskLists = new ArrayList<>();
        }
        return taskLists;
    }

    public List<Task> getTasksForTaskListEntry(String taskListId, String accessToken)
            throws IOException, GeneralSecurityException {
        this.tasksService = GTasksApiService.instantiateGapiService(accessToken);
        return this.getOpenTasksForTaskList(taskListId);
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


    public List<Task> getCompletedTasksForTaskList(String taskListId, ZonedDateTime newerThanDateTime)
            throws IOException {
        com.google.api.services.tasks.model.Tasks result = this.tasksService.tasks().list(taskListId)
                .setMaxResults(MAX_RESULTS)
                .setCompletedMin(convertZoneDateTimeToRFC3339Timestamp(newerThanDateTime))
                .setShowCompleted(true)
                .setShowHidden(true)
                .execute();
        List<Task> tasksForTaskList = result.getItems();
        if (tasksForTaskList == null) {
            tasksForTaskList = new ArrayList<>();
        }
        return tasksForTaskList;
    }

    public void instantiateGapiService(String accessToken) throws IOException, GeneralSecurityException {
        this.tasksService = GTasksApiService.instantiateGapiService(accessToken);
    }

    public List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime completedAfterDateTime)
            throws IOException {
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
                BaseTaskDto baseTaskDto = new BaseTaskDto.Builder()
                        .taskId(task.getId())
                        .title(task.getTitle())
                        .description(task.getNotes())
                        .completed(DateTimeHelper.convertGoogleTimeToDate(task.getCompleted()))
                        .source(GOOGLE_TASKS)
                        .addTags(taskList.getTitle())
                        .build();
                convertedTaskList.add(baseTaskDto);
            }
        }
        return convertedTaskList;
    }
}
