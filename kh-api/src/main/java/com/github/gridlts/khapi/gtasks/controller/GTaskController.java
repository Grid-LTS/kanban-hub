package com.github.gridlts.khapi.gtasks.controller;

import com.github.gridlts.khapi.gtasks.GTasksProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/gtasks")
public class GTaskController {
    private static final String APPLICATION_NAME = "Kaban Hub Client";

    private GTasksProperties gTasksProperties;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Autowired
    GTaskController(GTasksProperties gTasksProperties) {
        this.gTasksProperties = gTasksProperties;
    }

    @RequestMapping(value="/properties", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getProperties() {
        Map<String, String> gTasksConfig = new HashMap<String,String>();
        gTasksConfig.put("apiKey", gTasksProperties.getApiKey());
        gTasksConfig.put("scope", gTasksProperties.getScope());
        gTasksConfig.put("clientId", gTasksProperties.getClientId());
        return gTasksConfig;
    }

    @RequestMapping(value="/tasklists", method = RequestMethod.GET)
    @ResponseBody
    public List<TaskList> getTasklists(@RequestHeader(name="Authorization") String accessToken) throws IOException, GeneralSecurityException {
        accessToken = accessToken.substring(7);
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        TaskLists result = service.tasklists().list()
                .setMaxResults(10L)
                .execute();
        List<TaskList> taskLists = result.getItems();
        if (taskLists == null) {
            taskLists = new ArrayList<>();
        }
        return taskLists;
    }

}
