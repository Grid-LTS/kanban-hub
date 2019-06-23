package com.github.gridlts.khapi.taskw.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gridlts.khapi.dto.BaseTaskDto;
import com.github.gridlts.khapi.gtasks.service.DateTimeHelper;
import com.github.gridlts.khapi.taskw.dto.TaskwDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.gridlts.khapi.types.SourceManager.TASKWARRIOR;

@Service
public class TaskwRepo {

    private static final String COMPLETED_TASKS_CMD_FORMAT = "task status:completed end.after=%s export";
    private static final String PENDING_TASKS_CMD_FORMAT = "task status:pending export";

    @Value("${store.path}")
    private String storeDirectoryPath;

    // https://taskwarrior.org/docs/commands/export.html

    public List<TaskwDto> getCompletedTaskwTasks(ZonedDateTime newerThanDateTime) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                String.format(COMPLETED_TASKS_CMD_FORMAT, DateTimeHelper.convertZoneDateTimeToTaskwDate(newerThanDateTime))
                        .split(" "));
        builder.redirectErrorStream(true);
        builder.directory(new File(this.storeDirectoryPath));
        Process process = builder.start();
        ObjectMapper objectMapper = new ObjectMapper();
        List<TaskwDto> completedTaskw = new ArrayList<>();
        try (
                InputStream stdout = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
            StringBuffer taskwJson = new StringBuffer("");
            String line;
            while ((line = reader.readLine()) != null) {
                taskwJson.append(line);
            }
            completedTaskw = Arrays.asList(objectMapper.readValue(taskwJson.toString(), TaskwDto[].class));
        } catch (IOException ioException) {
            System.out.println(ioException.toString());
        }
        return completedTaskw;
    }

    public List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime newerThanDateTime) throws IOException {
        List<TaskwDto> taskwTasks = getCompletedTaskwTasks(newerThanDateTime);
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        for (TaskwDto taskwTask : taskwTasks) {
            // consistency checks before conversion for saving to file
            if (taskwTask.end() == null) {
                continue;
            }
            if (taskwTask.end().isBefore(newerThanDateTime)) {
                continue;
            }
            BaseTaskDto baseTaskDto = new BaseTaskDto.Builder()
                    .taskId(taskwTask.uuid())
                    .title(taskwTask.description())
                    .completed(taskwTask.end().toLocalDate())
                    .source(TASKWARRIOR)
                    .addAllTags(taskwTask.tags())
                    .projectCode(taskwTask.project())
                    .build();
            convertedTaskList.add(baseTaskDto);
        }
        return convertedTaskList;
    }

}
