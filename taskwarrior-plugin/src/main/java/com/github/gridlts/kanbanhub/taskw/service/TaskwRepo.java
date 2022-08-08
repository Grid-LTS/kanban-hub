package com.github.gridlts.kanbanhub.taskw.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceConfiguration;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.TaskResourceType;
import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import com.github.gridlts.kanbanhub.sources.api.dto.TaskListDto;
import com.github.gridlts.kanbanhub.taskw.TaskWarriorConfig;
import com.github.gridlts.kanbanhub.taskw.dto.TaskwDto;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.gridlts.kanbanhub.sources.api.TaskResourceType.TASKWARRIOR;
import static com.github.gridlts.kanbanhub.sources.api.TaskStatus.COMPLETED;
import static com.github.gridlts.kanbanhub.sources.api.TaskStatus.PENDING;

@Service
public class TaskwRepo implements ITaskResourceRepo {

    private static final String COMPLETED_TASKS_CMD_FORMAT = "task status:completed end.after=%s export";
    private static final String TASKS_CMD_FORMAT = "task modified.after=%s export";

    private TaskWarriorConfig taskWarriorConfig;

    public TaskwRepo(TaskWarriorConfig appConfig) {
        this.taskWarriorConfig = appConfig;
    }

    // https://taskwarrior.org/docs/commands/export.html

    @Override
    public void init(String token) {
    }

    @Override
    public void initConsole() {
    }

    @Override
    public TaskResourceType getResourceType() {
        return TASKWARRIOR;
    }

    @Override
    public ITaskResourceConfiguration getResourceConfiguration() {
        return null;
    }


    List<TaskwDto> getTasks(String command, ZonedDateTime newerThanDateTime) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                String.format(command, DateTimeHelper.convertZoneDateTimeToTaskwDate(newerThanDateTime))
                        .split(" "));
        builder.redirectErrorStream(true);
        builder.directory(new File(this.taskWarriorConfig.getStoreDirectoryPath()));
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

    @Override
    public List<BaseTaskDto> getAllTasksNewerThan(ZonedDateTime newerThanDateTime) {
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        List<TaskwDto> taskwTasks;
        try {
            taskwTasks = getTasks(TASKS_CMD_FORMAT, newerThanDateTime);
        } catch (IOException ex) {
            return convertedTaskList;
        }
        for (TaskwDto taskwTask : taskwTasks) {
            if (taskwTask.status().equals("deleted")) {
                continue;
            }
            BaseTaskDto baseTaskDto = mapToDto(taskwTask);
            convertedTaskList.add(baseTaskDto);
        }
        return convertedTaskList;
    }

    @Override
    public List<BaseTaskDto> getDeletedTasks(ZonedDateTime zonedDateTime) {
        return new ArrayList<>();
    }

    @Override
    public List<TaskListDto> getTaskListsEntry(String s) {
        throw new NotImplementedException();
    }

    @Override
    public List<BaseTaskDto> getOpenTasksForTaskListEntry(String s, String s1) {
        throw new NotImplementedException();
    }

    @Override
    public List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime completedAfterDateTime) {
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        List<TaskwDto> taskwTasks;
        try {
            taskwTasks = getTasks(COMPLETED_TASKS_CMD_FORMAT, completedAfterDateTime);
        } catch (IOException ex) {
            return convertedTaskList;
        }

        for (TaskwDto taskwTask : taskwTasks) {
            // consistency checks before conversion for saving to file
            if (taskwTask.end() == null) {
                continue;
            }
            if (taskwTask.end().isBefore(completedAfterDateTime)) {
                continue;
            }
            BaseTaskDto baseTaskDto = mapToDto(taskwTask);
            convertedTaskList.add(baseTaskDto);
        }
        return convertedTaskList;
    }

    private BaseTaskDto mapToDto(TaskwDto taskwTask) {
        TaskStatus status;
        switch (taskwTask.status()) {
            case "pending":
                status = PENDING;
                break;
            case "completed":
                status = COMPLETED;
                break;
            case "deleted":
                throw new IllegalStateException("Status deleted not allowed");
            default:
                status = PENDING;
        }
        LocalDate completedDate = taskwTask.end() != null ? taskwTask.end().toLocalDate() : null;
        return new BaseTaskDto.Builder()
                .taskId(taskwTask.uuid().toString())
                .title(taskwTask.description())
                .status(status)
                .creationDate(taskwTask.entry().toLocalDate())
                .completed(completedDate)
                .resource(TASKWARRIOR)
                .addAllTags(taskwTask.tags())
                .projectCode(taskwTask.project())
                .build();
    }

}
