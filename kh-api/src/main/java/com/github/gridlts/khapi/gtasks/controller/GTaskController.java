package com.github.gridlts.khapi.gtasks.controller;

import com.github.gridlts.khapi.gtasks.GTasksProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/gtasks")
public class GTaskController {

    private GTasksProperties gTasksProperties;

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
}
