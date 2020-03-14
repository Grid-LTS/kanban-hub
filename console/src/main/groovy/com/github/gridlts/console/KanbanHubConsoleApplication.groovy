package com.github.gridlts.console;

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(scanBasePackages = [
        "com.github.gridlts.console",
        "com.github.gridlts.console.google",
        "com.github.gridlts.khapi.config",
        "com.github.gridlts.khapi.service",
        "com.github.gridlts.khapi.repository",
        "com.github.gridlts.khapi.gtasks",
        "com.github.gridlts.khapi.taskw"])
@EntityScan("com.github.gridlts.khapi.model")
class KanbanHubConsoleApplication {

    static void main(String[] args) {
        SpringApplication.run(KanbanHubConsoleApplication, args)
    }
}
