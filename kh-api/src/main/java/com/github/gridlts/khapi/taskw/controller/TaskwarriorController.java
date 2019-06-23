package com.github.gridlts.khapi.taskw.controller;

import com.github.gridlts.khapi.taskw.service.TaskwRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping(path = "/taskw")
public class TaskwarriorController
{
    private TaskwRepo taskwRepo;

    @Autowired
    TaskwarriorController(TaskwRepo taskwRepo) {
        this.taskwRepo = taskwRepo;
    }

}
