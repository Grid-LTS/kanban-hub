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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
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
        this.metadata = new Properties();
    }

    public void saveAllCompletedTasks(String accessToken) throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        // initialize sessions of resources
        this.gTaskRepo.instantiateGapiService(accessToken);
        this.loadMetadataProperties();
        this.saveAllCompletedTasksForType(TaskResourceType.GOOGLE_TASKS);
        this.saveAllCompletedTasksForType(TaskResourceType.TASKWARRIOR);
        this.saveLastUpdatedTimes();
    }

    public void saveAllCompletedTasksForType(TaskResourceType resourceType) throws IOException, GeneralSecurityException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        Path completedFilePath = Paths.get(getCompletionFilePath(resourceType));
        if (Files.isDirectory(completedFilePath)) {
            throw new IOException(String.format("Cannot create file %s", completedFilePath));
        }
        Path parentDirectoryPath = completedFilePath.getParent();
        if (!Files.exists(parentDirectoryPath)) {
            Files.createDirectory(parentDirectoryPath);
        }
        Path metadataFilepath = Paths.get(getMetadataFilePath());
        if (!Files.exists(metadataFilepath)) {
            Files.deleteIfExists(completedFilePath);
        }
        String entryTemplate = "last_updated_%s";
        String timeEntry = String.format(entryTemplate, resourceType);
        Boolean hasNewTasks;
        if (Files.exists(metadataFilepath) && Files.exists(completedFilePath)) {
            ZonedDateTime lastSavedTime = getLastUpdatedTime(timeEntry);
            hasNewTasks = this.addRecentCompletedTasksForType(resourceType, lastSavedTime);
        } else {
            hasNewTasks = this.saveAllTasksInitial(resourceType);
        }
        if (hasNewTasks) {
            long unixTime = System.currentTimeMillis();
            this.metadata.setProperty(timeEntry, Long.toString(unixTime));
        }
    }


    public String getCompletionFilePath(TaskResourceType resourceType) {
        return this.storeDirectoryPath + "/" + String.format(COMPLETED_FILENAME_TEMPLATE, resourceType);
    }

    public String getMetadataFilePath() {
        return this.storeDirectoryPath + "/" + METADATA_TXT;
    }

    public boolean addRecentCompletedTasksForType(TaskResourceType resourceType, ZonedDateTime lastSavedTime) throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {

        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> recentTasks = resourceRepo.getAllCompletedTasksNewerThan(lastSavedTime);
        String append = this.writeToString(recentTasks);
        String completedFilename = getCompletionFilePath(resourceType);
        Files.write(Paths.get(completedFilename), append.getBytes(),
                StandardOpenOption.APPEND);
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

    public void loadMetadataProperties() throws IOException {
        //readout properties
        Path metadataFilepath = Paths.get(getMetadataFilePath());
        if (Files.exists(metadataFilepath)) {
            try (InputStream input = new FileInputStream(getMetadataFilePath())) {
                this.metadata.load(input);
            }
        }
    }

    public ZonedDateTime getLastUpdatedTime(String propertyName) throws IOException {
            String unixTimeString = this.metadata.getProperty(propertyName);
            long unixTime = Long.parseLong(unixTimeString, 10);
            return DateTimeHelper.convertUnixTimestampToZonedDateTime(unixTime);
    }

    public void saveLastUpdatedTimes() throws IOException {
        Path metadataFilepath = Paths.get(getMetadataFilePath());
        if (!Files.exists(metadataFilepath)) {
            Files.createFile(metadataFilepath);
        }
        try (OutputStream output = new FileOutputStream(getMetadataFilePath())) {
            this.metadata.store(output, null);
        }
    }

    public Boolean saveAllTasksInitial(TaskResourceType resourceType) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        ZonedDateTime startDateTime = DateTimeHelper.getOldEnoughDate();
        ITaskResourceRepo resourceRepo = getRepoForResourceType(resourceType);
        List<BaseTaskDto> initialTaskList = resourceRepo.getAllCompletedTasksNewerThan(startDateTime);
        this.writeToCSVFile(initialTaskList, getCompletionFilePath(resourceType));
        return initialTaskList.size() > 0;
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
