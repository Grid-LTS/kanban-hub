package com.github.gridlts.khapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = {"com.github.gridlts.khapi.controller", "com.github.gridlts.khapi.service",
        "com.github.gridlts.khapi.gtasks", "com.github.gridlts.khapi.taskw"})
@SpringBootApplication
public class KhapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhapiApplication.class, args);
    }

}

