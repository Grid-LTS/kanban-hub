package com.github.gridlts.console.google

import com.github.gridlts.khapi.gtasks.GTasksProperties
import com.github.gridlts.khapi.gtasks.service.GTaskRepo
import com.github.gridlts.khapi.gtasks.service.GTasksApiService
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.tasks.TasksScopes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GoogleAuthorization {

    FileDataStoreFactory dataStoreFactory

    NetHttpTransport httpTransport

    GTasksProperties gTaskConfig

    final File dataStoreDir

    Integer port = 7100

    @Autowired
    GoogleAuthorization(GTasksProperties gTaskConfig,
                        @Value('${application.fs.path}') String applicationFsPath) {
        this.gTaskConfig = gTaskConfig
        this.dataStoreDir = new File(applicationFsPath)
    }

    Credential authorize() throws Exception {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
        clientSecrets.setInstalled(new GoogleClientSecrets.Details().setClientId(gTaskConfig.clientId)
                .setClientSecret(gTaskConfig.clientKey))
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GTasksApiService.JSON_FACTORY, clientSecrets,
                Collections.singleton(TasksScopes.TASKS_READONLY)).setDataStoreFactory(
                dataStoreFactory).build()
        // authorize
        return new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver.Builder().setPort(port).build()).authorize("user")
    }

    Credential main() throws Exception {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            dataStoreFactory = new FileDataStoreFactory(this.dataStoreDir)
            // authorization
            return authorize()
        } catch (Exception e) {
            println("Something went wrong")
        }
    }
}