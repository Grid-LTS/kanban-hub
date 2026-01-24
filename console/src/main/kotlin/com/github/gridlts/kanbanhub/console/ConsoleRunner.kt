package com.github.gridlts.kanbanhub.console

import com.github.gridlts.kanbanhub.service.TaskCsvExport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ConsoleRunner @Autowired constructor(
    private val taskUpdateService: TaskUpdateService,
    private val taskCsvExport: TaskCsvExport
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val sourceArgs = args.sourceArgs.toMutableList()
        
        if (sourceArgs.contains("update")) {
            taskUpdateService.update()
            sourceArgs.remove("update")
        }
        
        if (sourceArgs.contains("export")) {
            sourceArgs.remove("export")
            if (args.sourceArgs.contains("--csv") || args.getOptionValues("csv") != null) {
                sourceArgs.remove("--csv")
                taskCsvExport.exportAllCompletedTasks()
            }
        }
        
        if (sourceArgs.isNotEmpty()) {
            val leftOverArgs = sourceArgs.joinToString(", ")
            println("Following arguments are not valid: $leftOverArgs")
        }
    }
}
