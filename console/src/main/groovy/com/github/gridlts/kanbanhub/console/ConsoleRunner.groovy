package com.github.gridlts.kanbanhub.console

import com.github.gridlts.kanbanhub.service.TaskCsvExport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ConsoleRunner implements ApplicationRunner {

    TaskUpdateService taskUpdateService

    com.github.gridlts.kanbanhub.service.TaskCsvExport taskCsvExport;

    @Autowired
    ConsoleRunner(TaskUpdateService taskUpdateService, TaskCsvExport taskCsvExport) {
        this.taskUpdateService = taskUpdateService;
        this.taskCsvExport = taskCsvExport;
    }

    @Override
    void run(ApplicationArguments args) throws Exception  {
        def sourceArgs = args.sourceArgs
        if (sourceArgs.contains("update")) {
            taskUpdateService.update()
            sourceArgs -= "update"
        }
        if (sourceArgs.contains("export")) {
            sourceArgs -= "export"
            if (args.sourceArgs.contains("--csv") || args.getOptionValues("csv")) {
                sourceArgs -= "--csv"
                taskCsvExport.exportAllCompletedTasks()
            }
        }
        if (sourceArgs.length > 0) {
            def leftOverArgs = sourceArgs.join(", ")
            println("Following arguments are not valid: ${leftOverArgs}")
        }
    }
}
