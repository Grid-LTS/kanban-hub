package com.github.gridlts.kanbanhub.console

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication(scanBasePackages = [
        "com.github.gridlts.kanbanhub.console",
        "com.github.gridlts.kanbanhub.config",
        "com.github.gridlts.kanbanhub.service",
        "com.github.gridlts.kanbanhub.repository",
        "com.github.gridlts.khapi.taskw"])
@EntityScan("com.github.gridlts.kanbanhub.model")
class KanbanHubConsoleApplication {

    static void main(String[] args) {
        SpringApplication.run(KanbanHubConsoleApplication, args)
    }
}
