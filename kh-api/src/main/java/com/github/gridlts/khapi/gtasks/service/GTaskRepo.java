package com.github.gridlts.khapi.gtasks.service;

import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.csv.CustomHeaderColumnNameMappingStrategy;
import com.github.gridlts.khapi.dto.ImmutableBaseTaskDto;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.github.gridlts.khapi.gtasks.service.DateTimeHelper.convertZoneDateTimeToRFC3339Timestamp;
import static com.github.gridlts.khapi.types.SourceManager.GOOGLE_TASKS;

@Service
public class GTaskRepo {

    private static final String COMPLETED_FILENAME = "completed.csv";
    private static final String METADATA_TXT = "metadata.txt";

    private static final Long MAX_RESULTS = 10000L;

    @Value("${store.path}")
    private String storeDirectoryPath;

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
            throws IOException, GeneralSecurityException {
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
            throws IOException, GeneralSecurityException {
        com.google.api.services.tasks.model.Tasks result = this.tasksService.tasks().list(taskListId)
                .setMaxResults(MAX_RESULTS)
                .setCompletedMin(convertZoneDateTimeToRFC3339Timestamp(newerThanDateTime))
                .setShowCompleted(true)
                .execute();
        List<Task> tasksForTaskList = result.getItems();
        if (tasksForTaskList == null) {
            tasksForTaskList = new ArrayList<>();
        }
        return tasksForTaskList;
    }

    public void saveAllCompletedTasks(String accessToken) throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        File f = new File(this.storeDirectoryPath + "/" + COMPLETED_FILENAME);
        if (f.isDirectory()) {
            throw new IOException(String.format("Cannot create file %s",
                    this.storeDirectoryPath + "/" + COMPLETED_FILENAME));
        }
        this.tasksService = GTasksApiService.instantiateGapiService(accessToken);
        if (f.exists()) {
            this.addRecentCompletedTasks();
        } else {
            this.saveAllTasksInitial();
        }
        try (OutputStream output = new FileOutputStream(this.storeDirectoryPath + "/" + METADATA_TXT)) {
            Properties metadata = new Properties();
            long unixTime = System.currentTimeMillis();
            metadata.setProperty("last_updated", Long.toString(unixTime));
            metadata.store(output, null);
        }
    }

    public void addRecentCompletedTasks() throws IOException, GeneralSecurityException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        //readout properties
        try (InputStream input = new FileInputStream(this.storeDirectoryPath + "/" + METADATA_TXT)) {
            Properties prop = new Properties();
            prop.load(input);
            String unixTimeString = prop.getProperty("last_updated");
            long unixTime = Long.parseLong(unixTimeString, 10);
            ZonedDateTime lastUpdatedTime = DateTimeHelper.convertUnixTimestampToZonedDateTime(unixTime);
            List<BaseTaskDto> recentThan = this.getAllCompletedTasksNewerThan(lastUpdatedTime);
            String append = this.writeToString(recentThan);
            Files.write(Paths.get(this.storeDirectoryPath + "/" + COMPLETED_FILENAME), append.getBytes(), StandardOpenOption.APPEND);
        }
    }

    public void saveAllTasksInitial() throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        List<BaseTaskDto> convertedTaskList = this.getAllCompletedTasksNewerThan(DateTimeHelper.getOldEnoughDate());
        this.writeToCSVFile(convertedTaskList);
    }

    private List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime newerThanDateTime)
            throws IOException, GeneralSecurityException {
        List<TaskList> taskLists = this.getTaskLists();
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        for (TaskList taskList : taskLists) {
            List<Task> tasks = this.getCompletedTasksForTaskList(taskList.getId(), newerThanDateTime);
            for (Task task : tasks) {
                // consistency checks before conversion for saving to file
                if (task.getCompleted() == null) {
                    continue;
                }
                ZonedDateTime taskDateTime = DateTimeHelper.convertGoogleTimeToZonedDateTime(task.getCompleted());
                if (taskDateTime.isBefore(newerThanDateTime)) {
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
        return convertedTaskList;
    }


    private void writeToCSVFile(List<BaseTaskDto> allTasks) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        Writer writer = new FileWriter(this.storeDirectoryPath + "/" + COMPLETED_FILENAME);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(allTasks);
        writer.close();
    }

    private String writeToString(List<BaseTaskDto> allTasks) throws
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        StringWriter writer = new StringWriter();
        // mapping of columns with their positions
        CustomHeaderColumnNameMappingStrategy<BaseTaskDto> mappingStrategy = new CustomHeaderColumnNameMappingStrategy<BaseTaskDto>();
        mappingStrategy.setType(ImmutableBaseTaskDto.class);
        StatefulBeanToCsv<BaseTaskDto> beanToCsv = new StatefulBeanToCsvBuilder<BaseTaskDto>(writer)
                .withMappingStrategy(mappingStrategy).build();
        beanToCsv.write(allTasks);
        return writer.toString();
    }
}
