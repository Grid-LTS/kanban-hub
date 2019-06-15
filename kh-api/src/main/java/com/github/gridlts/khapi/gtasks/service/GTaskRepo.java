package com.github.gridlts.khapi.gtasks.service;

import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.gridlts.khapi.types.SourceManager.GOOGLE_TASKS;

@Service
public class GTaskRepo {

    private static final String COMPLETED_FILENAME = "completed.csv";

    @Value("${store.path}")
    private String storeDirectoryPath;

    private GTasksApiService gTasksApiService;
    private Tasks tasksService;

    @Autowired
    GTaskRepo(GTasksApiService gTasksApiService) {
        this.gTasksApiService = gTasksApiService;
    }

    public List<TaskList> getTaskLists(String accessToken) throws IOException, GeneralSecurityException {
        this.tasksService = GTasksApiService.instantiateGapiService(accessToken);
        TaskLists result = this.tasksService.tasklists().list()
                .setMaxResults(10L)
                .execute();
        List<TaskList> taskLists = result.getItems();
        if (taskLists == null) {
            taskLists = new ArrayList<>();
        }
        return taskLists;
    }

    public List<Task> getTasksForTaskList(String taskListId, String accessToken, boolean isCompleted)
            throws IOException, GeneralSecurityException {
        this.tasksService = GTasksApiService.instantiateGapiService(accessToken);
        com.google.api.services.tasks.model.Tasks result = this.tasksService.tasks().list(taskListId)
                .setMaxResults(100L)
                .setShowCompleted(isCompleted)
                .execute();
        List<Task> tasksForTaskList = result.getItems();
        if (tasksForTaskList == null) {
            tasksForTaskList = new ArrayList<>();
        }
        return tasksForTaskList;
    }

    public void saveAllTasks(String accessToken)
            throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        List<TaskList> taskLists = this.getTaskLists(accessToken);
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        for (TaskList taskList : taskLists) {
            List<Task> tasks = this.getTasksForTaskList(taskList.getId(), accessToken, true);
            for (Task task : tasks) {
                if (task.getCompleted() == null) {
                    continue;
                }
                BaseTaskDto baseTaskDto = new BaseTaskDto.Builder()
                        .taskId(UUID.randomUUID())
                        .title(task.getTitle())
                        .description(task.getNotes())
                        .completed(DateTimeHelper.convertGoogleTimeToDate(task.getCompleted()))
                        .source(GOOGLE_TASKS)
                        .addTags(taskList.getTitle())
                        .build();
                convertedTaskList.add(baseTaskDto);

            }
        }
        this.writeToCSVFile(convertedTaskList);

    }

    private void writeToCSVFile(List<BaseTaskDto> allTasks) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        Writer writer = new FileWriter(this.storeDirectoryPath + "/" + COMPLETED_FILENAME);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(allTasks);
        writer.close();
    }

}
