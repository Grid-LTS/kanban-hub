package com.github.gridlts.kanbanhub.console

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication(scanBasePackages = [
        "com.github.gridlts.kanbanhub"])
@EntityScan("com.github.gridlts.kanbanhub.model")
class KanbanHubConsoleApplication {

    static void main(String[] args) {
        SpringApplication.run(KanbanHubConsoleApplication, args)
    }
}
