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
import java.util.List;
import java.util.Properties;

@Service
public class TaskRepo {

    private static final String COMPLETED_FILENAME = "completed.csv";
    private static final String METADATA_TXT = "metadata.txt";

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
        try (OutputStream output = new FileOutputStream(this.storeDirectoryPath + "/" + METADATA_TXT)) {
            Properties metadata = new Properties();
            long unixTime = System.currentTimeMillis();
            metadata.setProperty("last_updated", Long.toString(unixTime));
            metadata.store(output, null);
        }
    }

    public void addRecentCompletedTasks() throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException, GeneralSecurityException {
        //readout properties
        try (InputStream input = new FileInputStream(this.storeDirectoryPath + "/" + METADATA_TXT)) {
            Properties prop = new Properties();
            prop.load(input);
            String unixTimeString = prop.getProperty("last_updated");
            long unixTime = Long.parseLong(unixTimeString, 10);
            ZonedDateTime lastUpdatedTime = DateTimeHelper.convertUnixTimestampToZonedDateTime(unixTime);
            List<BaseTaskDto> recentTasks = this.gTaskRepo.getAllCompletedTasksNewerThan(lastUpdatedTime);
            List<BaseTaskDto> recentTaskwTasks = this.taskwRepo.getAllCompletedTasksNewerThan(lastUpdatedTime);
            recentTasks.addAll(recentTaskwTasks);
            String append = this.writeToString(recentTasks);
            Files.write(Paths.get(this.storeDirectoryPath + "/" + COMPLETED_FILENAME), append.getBytes(),
                    StandardOpenOption.APPEND);
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
