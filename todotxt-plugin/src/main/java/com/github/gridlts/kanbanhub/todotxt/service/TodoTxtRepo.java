package com.github.gridlts.kanbanhub.todotxt.service;

import com.github.gridlts.kanbanhub.helper.DateUtilities;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceConfiguration;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.TaskResourceType;
import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import com.github.gridlts.kanbanhub.sources.api.dto.TaskListDto;
import com.github.gridlts.kanbanhub.todotxt.TodoTxtConfig;
import com.github.gridlts.kanbanhub.todotxt.dto.TodoTxtDto;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static com.github.gridlts.kanbanhub.helper.DateUtilities.DATE_PATTERN;
import static com.github.gridlts.kanbanhub.sources.api.TaskResourceType.TODOTXT;
import static com.github.gridlts.kanbanhub.sources.api.TaskStatus.COMPLETED;
import static com.github.gridlts.kanbanhub.sources.api.TaskStatus.PENDING;

@Service
public class TodoTxtRepo implements ITaskResourceRepo {

    private final String completedTaskCommand;
    private final String pendingTaskCommand;
    // option -p prevents color encoded output
    private static final String TASKS_CMD_FORMAT = "todo.sh -p listfile %s";
    private static final String TASK_NOTE_CMD_FORMAT = "todo.sh note s %s";

    private TodoTxtConfig todoTxtConfig;

    public TodoTxtRepo(TodoTxtConfig appConfig) {
        this.todoTxtConfig = appConfig;
        this.completedTaskCommand = String.format(TASKS_CMD_FORMAT,
                appConfig.getDoneFileName());
        this.pendingTaskCommand = String.format(TASKS_CMD_FORMAT,
                appConfig.getPendingFileName());
    }

    @Override
    public void init(String token) {
    }

    @Override
    public void initConsole() {
    }

    @Override
    public TaskResourceType getResourceType() {
        return TODOTXT;
    }

    @Override
    public ITaskResourceConfiguration getResourceConfiguration() {
        return null;
    }

    @Override
    public List<BaseTaskDto> getAllTasksNewerThan(ZonedDateTime newerThanDateTime) {
        List<BaseTaskDto> convertedTaskList = new ArrayList<>();
        List<TodoTxtDto> todoTxtTasks;
        try {
            todoTxtTasks = getTasks(pendingTaskCommand, newerThanDateTime);
            todoTxtTasks.addAll(getTasks(completedTaskCommand, newerThanDateTime));
        } catch (IOException ex) {
            return convertedTaskList;
        }
        for (TodoTxtDto todoTxtTask : todoTxtTasks) {
            if (todoTxtTask.status().equals("deleted")) {
                continue;
            }
            BaseTaskDto baseTaskDto = mapToDto(todoTxtTask);
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
        List<TodoTxtDto> todoTxtTasks;
        try {
            todoTxtTasks = getTasks(completedTaskCommand, completedAfterDateTime);
        } catch (IOException ex) {
            return convertedTaskList;
        }

        for (TodoTxtDto todoTxtTask : todoTxtTasks) {
            // consistency checks before conversion for saving to file
            if (todoTxtTask.end() == null) {
                continue;
            }
            if (todoTxtTask.end().isBefore(completedAfterDateTime)) {
                continue;
            }
            BaseTaskDto baseTaskDto = mapToDto(todoTxtTask);
            convertedTaskList.add(baseTaskDto);
        }
        return convertedTaskList;
    }

    List<TodoTxtDto> getTasks(String command, ZonedDateTime newerThanDateTime) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                command.split(" "));
        builder.redirectErrorStream(true);
        builder.directory(new File(this.todoTxtConfig.getStoreDirectoryPath()));
        Process process = builder.start();
        List<TodoTxtDto> completedTodoTxt = new ArrayList<>();
        try (
                InputStream stdout = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--")) {
                    break;
                }
                TodoTxtDto todoTxtDto = mapTodoTxtDto(line);
                newerThanDateTime = newerThanDateTime.toLocalDate().atStartOfDay(ZoneId.of("UTC"));
                if (todoTxtDto.modified().isAfter(newerThanDateTime)
                    || todoTxtDto.modified().isEqual(newerThanDateTime)) {
                    completedTodoTxt.add(todoTxtDto);
                }

            }
        } catch (IOException ioException) {
            System.out.println(ioException.toString());
        }
        return completedTodoTxt;
    }

    private String readDescription(String taskId) {
        String command = String.format(TASK_NOTE_CMD_FORMAT, taskId);
        ProcessBuilder builder = new ProcessBuilder(
                command.split(" "));
        builder.redirectErrorStream(true);
        builder.directory(new File(this.todoTxtConfig.getStoreDirectoryPath()));
        Process process;
        try {
            process = builder.start();
        } catch (IOException ioException) {
            System.out.println(ioException);
            return "";
        }
        try (
                InputStream stdout = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
            String line;
            StringBuilder desc = new StringBuilder("");
            reader.readLine(); // omit first line
            while ((line = reader.readLine()) != null) {
                desc.append(line.trim());
                desc.append(System.lineSeparator());
            }
            return desc.toString();
        } catch (IOException ioException) {
            System.out.println(ioException);

        }
        return "";
    }

    private TodoTxtDto mapTodoTxtDto(String line) {
        ZonedDateTime creationDate = ZonedDateTime.now();
        ZonedDateTime completionDate = null;
        TaskStatus status = PENDING;
        String project = "";
        String description = "";
        List<String> tags = new ArrayList<>();
        List<String> titleBits = new ArrayList<>();
        List<String> bits = new LinkedList<>(Arrays.asList(line.split(" ")));
        String taskId = "";
        if (!bits.isEmpty() && bits.get(0).matches("\\d+")) {
            // we will ignore id's
            taskId = bits.get(0);
            bits.remove(0);
        }
        for (String bit : bits) {
            if ("x".equals(bit)) {
                status = COMPLETED;
                continue;
            }
            final Pattern DATE_PATTERN = Pattern.compile(
                    "^\\d{4}-\\d{2}-\\d{2}$");
            if (DATE_PATTERN.matcher(bit).matches()) {
                if (status == COMPLETED && completionDate == null) {
                    completionDate = DateUtilities.convert(bit).atStartOfDay(ZoneId.of("UTC"));
                    continue;
                }
                creationDate = DateUtilities.convert(bit).atStartOfDay(ZoneId.of("UTC"));
                continue;
            }
            // project tags and context tags follow notation given in https://github.com/todotxt/todo.txt
            if (bit.startsWith("+")) {
                if (!project.isEmpty()) {
                    continue;
                }
                project = bit.substring(1);
                continue;
            }
            if (bit.startsWith("@")) {
                tags.add(bit.substring(1));
                continue;
            }
            if (bit.startsWith("note:")) {
                if (status == COMPLETED) {
                    continue;
                }
                if (taskId.isEmpty()) {
                    continue;
                }
                description = readDescription(taskId);
                continue;
            }
            titleBits.add(bit);
        }
        String title = String.join(" ", titleBits);
        String id = creationDate.format(DATE_PATTERN) + "|" + title;
        return new TodoTxtDto.Builder()
                .id(id)
                .entry(creationDate)
                .modified(status == COMPLETED ? completionDate : ZonedDateTime.now())
                .end(completionDate)
                .title(title)
                .description(description)
                .status(status)
                .project(project)
                .addAllTags(tags)
                .build();
    }

    private BaseTaskDto mapToDto(TodoTxtDto todoTxtTask) {
        LocalDate completedDate = todoTxtTask.end() != null ?
                Objects.requireNonNull(todoTxtTask.end()).toLocalDate() : null;
        return new BaseTaskDto.Builder()
                .taskId(todoTxtTask.id())
                .title(todoTxtTask.title())
                .description(todoTxtTask.description())
                .status(todoTxtTask.status())
                .creationDate(todoTxtTask.entry().toLocalDate())
                .completed(completedDate)
                .resource(TODOTXT)
                .addAllTags(todoTxtTask.tags())
                .projectCode(todoTxtTask.project())
                .build();
    }

}
