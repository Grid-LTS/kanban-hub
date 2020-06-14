package com.github.gridlts.kanbanhub.console

import com.github.gridlts.kanbanhub.service.TaskDbRepo
import org.springframework.stereotype.Component

@Component
class TaskUpdateService {

    TaskDbRepo taskDbRepo

    TaskUpdateService(TaskDbRepo taskDbRepo) {
        this.taskDbRepo = taskDbRepo
    }

    void update() {
        taskDbRepo.saveAllRecentTasksConsole()
    }
}



