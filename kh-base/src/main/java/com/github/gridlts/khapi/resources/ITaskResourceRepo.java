package com.github.gridlts.khapi.resources;

import com.github.gridlts.khapi.dto.BaseTaskDto;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

public interface ITaskResourceRepo {
    public List<BaseTaskDto> getAllCompletedTasksNewerThan(ZonedDateTime completedAfterDateTime)
            throws IOException;
}
