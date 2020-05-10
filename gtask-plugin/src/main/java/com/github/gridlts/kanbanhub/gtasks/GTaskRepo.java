package com.github.gridlts.kanbanhub.gtasks;

import com.github.gridlts.kanbanhub.gtasks.service.GTasksApiService;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.github.gridlts.kanbanhub.gtasks.DateTimeHelper.convertZoneDateTimeToRFC3339Timestamp;
import static com.github.gridlts.kanbanhub.sources.api.TaskResourceType.GOOGLE_TASKS;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GTaskRepo implements ITaskResourceRepo {

    private static final Long MAX_RESULTS = 10000L;

    private Tasks tasksService;
    private GTasksApiService gTasksApiService;

    public GTaskRepo(GTasksApiService gTasksApiService) {
        this.gTasksApiService = gTasksApiService;
    }

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
    public void initConsole(){
        this.tasksService = this.gTasksApiService.instantiateGapiServiceConsole();
    }

    @Override
    public String getResourceType() {
        return "google_tasks";
    }

    public List<TaskList> getTaskListsEntry(String accessToken) throws IOException, GeneralSecurityException {
        this.tasksService = gTasksApiService.instantiateGapiService(accessToken);
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
        this.tasksService = gTasksApiService.instantiateGapiService(accessToken);
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