package com.github.gridlts.console

import com.github.gridlts.khapi.service.TaskCsvExport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ConsoleRunner implements ApplicationRunner {

    TaskUpdateService taskUpdateService

    TaskCsvExport taskCsvExport;

    @Autowired
    ConsoleRunner(TaskUpdateService taskUpdateService,  TaskCsvExport taskCsvExport) {
        this.taskUpdateService = taskUpdateService;
        this.taskCsvExport = taskCsvExport;
    }

    @Override
    void run(ApplicationArguments args) throws Exception  {
        if (args.sourceArgs.contains("update")) {
            taskUpdateService.update()
        }
        if (args.sourceArgs.contains("export")) {
            if (args.sourceArgs.contains("--csv") || args.getOptionValues("csv")) {
                taskCsvExport.exportAllCompletedTasks()
            }
        }
    }
}
