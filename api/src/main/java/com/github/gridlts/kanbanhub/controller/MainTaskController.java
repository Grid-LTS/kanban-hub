package com.github.gridlts.kanbanhub.controller;

import com.github.gridlts.kanbanhub.service.TaskDbRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainTaskController {

    private TaskDbRepo taskRepo;

    @Autowired
    MainTaskController(TaskDbRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    @RequestMapping(value = "/save/all", method = RequestMethod.POST)
    @ResponseBody
    public void saveAllTasks(@RequestHeader(name = "Authorization") String gTasksAccessToken) {
       // taskRepo.saveAllCompletedTasks(gTasksAccessToken);
    }
}
