package com.github.gridlts.console

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ConsoleRunner implements CommandLineRunner {

    @Autowired
    TaskUpdateService taskUpdateService

    @Override
    void run(String... args) {
        taskUpdateService.update()
    }
}
