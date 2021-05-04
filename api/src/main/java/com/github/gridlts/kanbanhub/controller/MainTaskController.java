package com.github.gridlts.kanbanhub.controller;

import com.github.gridlts.kanbanhub.exception.ResourceNotFoundException;
import com.github.gridlts.kanbanhub.service.TaskDbRepo;
import com.github.gridlts.kanbanhub.sources.api.ITaskResourceRepo;
import com.github.gridlts.kanbanhub.sources.api.dto.BaseTaskDto;
import com.github.gridlts.kanbanhub.sources.api.dto.TaskListDto;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class MainTaskController {

    private TaskDbRepo taskRepo;
    private Map<String, ITaskResourceRepo> repos;

    @Autowired
    MainTaskController(TaskDbRepo taskRepo, List<ITaskResourceRepo> repos) {
        this.taskRepo = taskRepo;
        this.repos = new HashedMap<>();
        for (ITaskResourceRepo repo : repos) {
            this.repos.put(repo.getResourceType(), repo);
        }
    }

    @RequestMapping(value = "/{resource}/save", method = RequestMethod.POST)
    public void saveAllTasks(@RequestHeader(name = "Authorization") String accessToken,
                             @PathVariable String resource) {
        if (!repos.containsKey(resource)) {
            throw new ResourceNotFoundException(resource);
        }
    }

    @RequestMapping(value = "/{resourceId}/properties", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public Map<String, String> getProperties(@PathVariable String resourceId) {
        return retrieveResourceRepoById(resourceId).getResourceConfiguration().getProperties();
    }

    @RequestMapping(value = "/{resourceId}/tasklists", method = RequestMethod.GET)
    public List<TaskListDto> getTasklists(@PathVariable String resourceId,
                                          @RequestHeader(name = "Authorization")
                                                  String accessToken) {
        return retrieveResourceRepoById(resourceId).getTaskListsEntry(accessToken);
    }

    @RequestMapping(value = "/{resourceId}/{taskListId}/tasks", method = RequestMethod.GET)
    public List<BaseTaskDto> getTasksForTaskList(@PathVariable String resourceId,
                                                 @PathVariable String taskListId,
                                                 @RequestHeader(name = "Authorization")
                                                         String accessToken) {
        return retrieveResourceRepoById(resourceId).getOpenTasksForTaskListEntry(taskListId, accessToken);
    }

    private ITaskResourceRepo retrieveResourceRepoById(String resourceId) {
        if (!repos.containsKey(resourceId)) {
            throw new ResourceNotFoundException(resourceId);
        }
        return repos.get(resourceId);
    }

}
