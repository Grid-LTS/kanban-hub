package com.github.gridlts.khapi.gtasks.controller;

import com.github.gridlts.khapi.gtasks.GTasksProperties;
import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/gtasks")
public class GTaskController {

    private GTasksProperties gTasksProperties;
    private GTaskRepo gTaskRepo;

    @Autowired
    GTaskController(GTasksProperties gTasksProperties, GTaskRepo gTaskRepo) {
        this.gTasksProperties = gTasksProperties;
        this.gTaskRepo = gTaskRepo;
    }

    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getProperties() {
        Map<String, String> gTasksConfig = new HashMap<>();
        gTasksConfig.put("apiKey", gTasksProperties.getApiKey());
        gTasksConfig.put("scope", gTasksProperties.getScope());
        gTasksConfig.put("clientId", gTasksProperties.getClientId());
        return gTasksConfig;
    }

    @RequestMapping(value = "/tasklists", method = RequestMethod.GET)
    @ResponseBody
    public List<TaskList> getTasklists(@RequestHeader(name = "Authorization") String accessToken) throws IOException, GeneralSecurityException {
        return gTaskRepo.getTaskListsEntry(accessToken);
    }

    @RequestMapping(value = "/{taskListId}/tasks", method = RequestMethod.GET)
    @ResponseBody
    public List<Task> getTasksForTaskList(@PathVariable String taskListId,
                                          @RequestHeader(name = "Authorization") String accessToken)
            throws IOException, GeneralSecurityException {
        return gTaskRepo.getTasksForTaskListEntry(taskListId, accessToken);
    }

}
