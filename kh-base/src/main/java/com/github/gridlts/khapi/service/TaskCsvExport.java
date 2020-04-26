package com.github.gridlts.khapi.service;

import com.github.gridlts.khapi.config.AppConfig;
import com.github.gridlts.khapi.csv.CustomHeaderColumnNameMappingStrategy;
import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.dto.ImmutableBaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.resources.TaskResourceType;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

@Component
public class TaskCsvExport {
    private static final String COMPLETED_FILENAME_TEMPLATE = "%s_completed.csv";

    private String storeDirectoryPath;

    private TaskDbRepo taskDbRepo;

    @Autowired
    TaskCsvExport(AppConfig appConfig, TaskDbRepo taskDbRepo) {
        this.storeDirectoryPath = appConfig.getStoreDirectoryPath();
        this.taskDbRepo = taskDbRepo;
    }

    public String getCompletionFilePath(TaskResourceType resourceType) {
        return this.storeDirectoryPath + "/" + String.format(COMPLETED_FILENAME_TEMPLATE, resourceType);
    }

    public void exportAllCompletedTasks() throws CsvRequiredFieldEmptyException, IOException, CsvDataTypeMismatchException {
        exportAllCompletedTasksForType(TaskResourceType.GOOGLE_TASKS);
        exportAllCompletedTasksForType(TaskResourceType.TASKWARRIOR);
    }

    public void exportAllCompletedTasksForType(TaskResourceType resourceType) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        boolean isInitial;
        Path completedFilePath = Paths.get(getCompletionFilePath(resourceType));
        Instant lastUpdatedDate;
        if (Files.exists(completedFilePath)) {
            lastUpdatedDate = Files.getLastModifiedTime(completedFilePath).toInstant();
            isInitial = false;
        } else {
            isInitial = true;
            lastUpdatedDate = DateTimeHelper.getOldEnoughDate().toInstant();
        }
        List<BaseTaskDto> newTasks = taskDbRepo.getAllTasksInsertedBefore(resourceType, lastUpdatedDate);
        if (newTasks.size() == 0) {
            return;
        }
        if (isInitial) {
            createExportFile(resourceType);
            this.exportAllTasksInitial(resourceType, newTasks);
        } else {
            this.addRecentCompletedTasksForType(resourceType, newTasks);
        }
    }

    public void addRecentCompletedTasksForType(TaskResourceType resourceType, List<BaseTaskDto> recentTasks)
            throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {
        String append = this.writeToString(recentTasks);
        String completedFilename = getCompletionFilePath(resourceType);
        Files.write(Paths.get(completedFilename), append.getBytes(),
                StandardOpenOption.APPEND);
    }

    public void exportAllTasksInitial(TaskResourceType resourceType, List<BaseTaskDto> initialTaskList) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        this.writeToCSVFile(initialTaskList, getCompletionFilePath(resourceType));
    }

    private void createExportFile(TaskResourceType resourceType) throws IOException {
        Path completedFilePath = Paths.get(getCompletionFilePath(resourceType));
        if (Files.isDirectory(completedFilePath)) {
            throw new IOException(String.format("Cannot create file %s", completedFilePath));
        }
        Path parentDirectoryPath = completedFilePath.getParent();
        if (!Files.exists(parentDirectoryPath)) {
            Files.createDirectory(parentDirectoryPath);
        }
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
