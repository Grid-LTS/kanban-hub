package com.github.gridlts.khapi.controller;

import com.github.gridlts.khapi.gtasks.service.GTaskRepo;
import com.github.gridlts.khapi.service.TaskRepo;
import com.github.gridlts.khapi.taskw.dto.TaskwDto;
import com.github.gridlts.khapi.taskw.service.TaskwRepo;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class MainTaskController {

    private TaskRepo taskRepo;

    @Autowired
    MainTaskController(TaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    @RequestMapping(value = "/save/all", method = RequestMethod.POST)
    @ResponseBody
    public void saveAllTasks(@RequestHeader(name = "Authorization") String gTasksAccessToken) throws IOException,
            GeneralSecurityException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        taskRepo.saveAllCompletedTasks(gTasksAccessToken);
    }
}
