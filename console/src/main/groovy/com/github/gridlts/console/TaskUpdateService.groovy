package com.github.gridlts.console

import com.github.gridlts.console.google.GoogleAuthorization
import com.github.gridlts.khapi.service.TaskDbRepo
import org.springframework.stereotype.Component

@Component
class TaskUpdateService {

    GoogleAuthorization googleAuth

    TaskDbRepo taskDbRepo

    TaskUpdateService(GoogleAuthorization googleAuth,
                      TaskDbRepo taskDbRepo) {
        this.googleAuth = googleAuth
        this.taskDbRepo = taskDbRepo
    }

    void update() {
        def auth = googleAuth.main()
        taskDbRepo.saveAllCompletedTasks(auth.accessToken)
    }
}



