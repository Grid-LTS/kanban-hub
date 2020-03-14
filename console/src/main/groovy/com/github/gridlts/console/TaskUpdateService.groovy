package com.github.gridlts.console

import com.github.gridlts.console.google.GoogleAuthorization
import org.springframework.stereotype.Component

@Component
class TaskUpdateService {

    GoogleAuthorization googleAuth

    TaskUpdateService(GoogleAuthorization googleAuth) {
        this.googleAuth = googleAuth
    }

    void update() {
        def auth = googleAuth.main()
        println(auth)
    }
}



