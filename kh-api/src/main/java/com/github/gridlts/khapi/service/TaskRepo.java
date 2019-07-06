package com.github.gridlts.khapi.service;

import com.github.gridlts.khapi.csv.CustomHeaderColumnNameMappingStrategy;
import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.dto.ImmutableBaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.github.gridlts.khapi.taskw.service.TaskwRepo;
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

    private static final String COMPLETED_FILENAME = "completed.csv";
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
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        File f = new File(this.storeDirectoryPath + "/" + COMPLETED_FILENAME);
        if (f.isDirectory()) {
            throw new IOException(String.format("Cannot create file %s",
                    this.storeDirectoryPath + "/" + COMPLETED_FILENAME));
        }
        // initialize sessions with the services
        this.gTaskRepo.instantiateGapiService(accessToken);
        if (f.exists()) {
            this.addRecentCompletedTasks();
        } else {
            this.saveAllTasksInitial();
        }
    }

    public void addRecentCompletedTasks() throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException, GeneralSecurityException {
        String entryTemplate = "last_updated_%s";
        String gTaskTimeEntry = String.format(entryTemplate, "gtask");
        String taskwTimeEntry = String.format(entryTemplate, "taskw");
        ZonedDateTime gTaskedLastSavedTime = getLastUpdatedTime(gTaskTimeEntry);
        List<BaseTaskDto> recentTasks = new ArrayList<>();
        List<BaseTaskDto> recentGTaskTasks = this.gTaskRepo.getAllCompletedTasksNewerThan(gTaskedLastSavedTime);
        ZonedDateTime taskwLastSavedTime = getLastUpdatedTime(taskwTimeEntry);
        List<BaseTaskDto> recentTaskwTasks = this.taskwRepo.getAllCompletedTasksNewerThan(taskwLastSavedTime);
        recentTasks.addAll(recentGTaskTasks);
        recentTasks.addAll(recentTaskwTasks);
        String append = this.writeToString(recentTasks);
        Files.write(Paths.get(this.storeDirectoryPath + "/" + COMPLETED_FILENAME), append.getBytes(),
                StandardOpenOption.APPEND);
        if (recentGTaskTasks.size() > 0) {
            saveLastUpdatedTime(gTaskTimeEntry);
        }
        if (recentTaskwTasks.size() > 0) {
            saveLastUpdatedTime(taskwTimeEntry);
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


    public void saveAllTasksInitial() throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        ZonedDateTime startDateTime = DateTimeHelper.getOldEnoughDate();
        List<BaseTaskDto> initialTaskList = this.gTaskRepo.getAllCompletedTasksNewerThan(startDateTime);
        List<BaseTaskDto> initialTaskwList = this.taskwRepo.getAllCompletedTasksNewerThan(startDateTime);
        initialTaskList.addAll(initialTaskwList);
        this.writeToCSVFile(initialTaskList);
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

    private void writeToCSVFile(List<BaseTaskDto> allTasks) throws IOException,
            CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        Writer writer = new FileWriter(this.storeDirectoryPath + "/" + COMPLETED_FILENAME);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(allTasks);
        writer.close();
    }

}
