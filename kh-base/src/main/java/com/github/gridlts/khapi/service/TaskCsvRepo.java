package com.github.gridlts.khapi.service;

import com.github.gridlts.khapi.config.AppConfig;
import com.github.gridlts.khapi.csv.CustomHeaderColumnNameMappingStrategy;
import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.dto.ImmutableBaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.github.gridlts.khapi.resources.ITaskResourceRepo;
import com.github.gridlts.khapi.resources.TaskResourceType;
import com.github.gridlts.khapi.taskw.service.TaskwRepo;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;

public class TaskCsvRepo {
    private static final String COMPLETED_FILENAME_TEMPLATE = "%s_completed.csv";
    private static final String METADATA_TXT = "metadata.txt";

    private String storeDirectoryPath;

    private Properties metadata;

    @Autowired
    TaskCsvRepo(AppConfig appConfig) {
        this.metadata = new Properties();
        this.storeDirectoryPath = appConfig.getStoreDirectoryPath();
    }

    public String getCompletionFilePath(TaskResourceType resourceType) {
        return this.storeDirectoryPath + "/" + String.format(COMPLETED_FILENAME_TEMPLATE, resourceType);
    }

    public void saveAllCompletedTasksForType(TaskResourceType resourceType, List<BaseTaskDto> tasks) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        Path completedFilePath = Paths.get(getCompletionFilePath(resourceType));
        if (Files.isDirectory(completedFilePath)) {
            throw new IOException(String.format("Cannot create file %s", completedFilePath));
        }
        Path parentDirectoryPath = completedFilePath.getParent();
        if (!Files.exists(parentDirectoryPath)) {
            Files.createDirectory(parentDirectoryPath);
        }
        String entryTemplate = "last_updated_%s";
        String timeEntry = String.format(entryTemplate, resourceType);
        Boolean hasNewTasks;
        if (Files.exists(completedFilePath)) {
            hasNewTasks = this.addRecentCompletedTasksForType(resourceType, tasks);
        } else {
            hasNewTasks = this.saveAllTasksInitial(resourceType, tasks);
        }
        if (hasNewTasks) {
            long unixTime = System.currentTimeMillis();
            this.metadata.setProperty(timeEntry, Long.toString(unixTime));
        }
    }

    public boolean addRecentCompletedTasksForType(TaskResourceType resourceType, List<BaseTaskDto> recentTasks)
            throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {
        String append = this.writeToString(recentTasks);
        String completedFilename = getCompletionFilePath(resourceType);
        Files.write(Paths.get(completedFilename), append.getBytes(),
                StandardOpenOption.APPEND);
        return recentTasks.size() > 0;
    }

    public Boolean saveAllTasksInitial(TaskResourceType resourceType, List<BaseTaskDto> initialTaskList) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
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
