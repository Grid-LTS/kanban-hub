package com.github.gridlts.kanbanhub.console

import com.github.gridlts.kanbanhub.service.TaskDbRepo
import org.springframework.stereotype.Component

@Component
class TaskUpdateService(
    private val taskDbRepo: TaskDbRepo
) {

    fun update() {
        taskDbRepo.saveAllRecentTasksConsole()
    }
}
