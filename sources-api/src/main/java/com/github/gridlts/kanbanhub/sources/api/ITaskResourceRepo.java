package com.github.gridlts.kanbanhub.sources.api;

import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

public interface ITaskResourceRepo {
    List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime completedAfterDateTime);

    List<BaseTaskDto> getAllTasksNewerThan(ZonedDateTime insertedAfterDateTime);

    void init(String accessToken);

    void initConsole();

    String getResourceType();
}
