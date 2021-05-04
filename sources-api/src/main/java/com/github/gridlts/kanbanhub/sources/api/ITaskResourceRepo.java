package com.github.gridlts.kanbanhub.sources.api;

import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import com.github.gridlts.kanbanhub.sources.api.dto.TaskListDto;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

public interface ITaskResourceRepo {

    List<TaskListDto> getTaskListsEntry(String accessToken);

    List<BaseTaskDto> getOpenTasksForTaskListEntry(String taskListId, String accessToken);

    List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime completedAfterDateTime);

    List<BaseTaskDto> getAllTasksNewerThan(ZonedDateTime insertedAfterDateTime);

    void init(String accessToken);

    void initConsole();

    String getResourceType();

    ITaskResourceConfiguration getResourceConfiguration();

}
