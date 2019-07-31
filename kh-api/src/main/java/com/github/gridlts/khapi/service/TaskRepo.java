package com.github.gridlts.khapi.service;

import com.github.gridlts.khapi.csv.CustomHeaderColumnNameMappingStrategy;
import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.dto.ImmutableBaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.github.gridlts.khapi.resources.ITaskResourceRepo;
import com.github.gridlts.khapi.taskw.service.TaskwRepo;
import com.github.gridlts.khapi.resources.TaskResourceType;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class TaskRepo {

    private static final String COMPLETED_FILENAME_TEMPLATE = "%s_completed.csv";
    private static final String METADATA_TXT = "metadata.txt";

    private Properties metadata;
    @Value("${store.path}")
    private String storeDirectoryPath;

    private GTaskRepo gTaskRepo;
    private TaskwRepo taskwRepo;


    @Autowired
    TaskRepo(GTaskRepo gTaskRepo, TaskwRepo taskwRepo) {
        this.gTaskRepo = gTaskRepo;
        this.taskwRepo = taskwRepo;
    }

    public void saveAllCompletedTasks(String accessToken) throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException  {
        // initialize sessions of resources
        this.gTaskRepo.instantiateGapiService(accessToken);

        this.saveAllCompletedTasksForType(TaskResourceType.GOOGLE_TASKS);
        this.saveAllCompletedTasksForType(TaskResourceType.TASKWARRIOR);
    }

    public void saveAllCompletedTasksForType(TaskResourceType resourceType) throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        String completedFilePath = getCompletionFilePath(resourceType);
        File f = new File(completedFilePath);
        if (f.isDirectory()) {
            throw new IOException(String.format("Cannot create file %s", completedFilePath));
        }
        if (f.exists()) {
            this.addRecentCompletedTasksForType(resourceType);
        } else {
            this.saveAllTasksInitial(resourceType);
        }
    }


    public String getCompletionFilePath(TaskResourceType resourceType) {
        return  this.storeDirectoryPath + "/" + String.format(COMPLETED_FILENAME_TEMPLATE, resourceType);
    }

    public void addRecentCompletedTasksForType(TaskResourceType resourceType) throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException, GeneralSecurityException {
        String entryTemplate = "last_updated_%s";
        String timeEntry = String.format(entryTemplate, resourceType);
        ZonedDateTime lastSavedTime = getLastUpdatedTime(timeEntry);
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllCompletedTasksNewerThan(lastSavedTime);
        String append = this.writeToString(recentTasks);
        String completedFilename = getCompletionFilePath(resourceType);
        Files.write(Paths.get(this.storeDirectoryPath + "/" + completedFilename), append.getBytes(),
                StandardOpenOption.APPEND);
        if (recentTasks.size() > 0) {
            saveLastUpdatedTime(timeEntry);
        }
    }

    public ITaskResourceRepo getRepoForResourceType(TaskResourceType resourceType) {
        switch (resourceType) {
            case TASKWARRIOR: return this.taskwRepo;
            case GOOGLE_TASKS: return this.gTaskRepo;
            default: throw new RuntimeException("Unknown Resource type");
        }
    }


    public ZonedDateTime getLastUpdatedTime(String propertyName) throws IOException {
        //readout properties
        try (InputStream input = new FileInputStream(this.storeDirectoryPath + "/" + METADATA_TXT)) {
            this.metadata = new Properties();
            this.metadata.load(input);
            String unixTimeString = this.metadata.getProperty(propertyName);
            long unixTime = Long.parseLong(unixTimeString, 10);
            return DateTimeHelper.convertUnixTimestampToZonedDateTime(unixTime);
        }
    }

    public void saveLastUpdatedTime(String propertyName) throws IOException {
        try (OutputStream output = new FileOutputStream(this.storeDirectoryPath + "/" + METADATA_TXT)) {
            long unixTime = System.currentTimeMillis();
            this.metadata.setProperty(propertyName, Long.toString(unixTime));
            this.metadata.store(output, null);
        }
    }


    public void saveAllTasksInitial(TaskResourceType resourceType) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        ZonedDateTime startDateTime = DateTimeHelper.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllCompletedTasksNewerThan(startDateTime);
        this.writeToCSVFile(initialTaskList, getCompletionFilePath(resourceType));
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

    private void writeToCSVFile(List<BaseTaskDto> allTasks, String filePath) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        Writer writer = new FileWriter(filePath);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(allTasks);
        writer.close();
    }

}
