package com.github.gridlts.khapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(scanBasePackages={"com.github.gridlts.khapi.controller",
        "com.github.gridlts.khapi.config",
        "com.github.gridlts.khapi.service",
        "com.github.gridlts.khapi.gtasks", "com.github.gridlts.khapi.taskw"})
public class KhapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhapiApplication.class, args);
    }

}

