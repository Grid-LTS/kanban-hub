package com.github.gridlts.kanbanhub.gtasks.service;

import com.github.gridlts.kanbanhub.gtasks.GTasksProperties;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GTasksApiService {

    public static final String APPLICATION_NAME = "Kaban Hub Client";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private String accessToken = "";
    private Tasks tasksService;
    private FileDataStoreFactory dataStoreFactory;
    private NetHttpTransport httpTransport;
    private GTasksProperties gTaskConfig;

    final File dataStoreDir;

    private Integer port = 7100;
    private String gTasksRootUrl;

    @Autowired
    public GTasksApiService(GTasksProperties gTaskConfig,
                            @Value("${application.fs.path}") String applicationFsPath,
                            @Value("${gtasks.url.root}") String gTaskRootUrl) {
        this.gTaskConfig = gTaskConfig;
        this.dataStoreDir = new File(applicationFsPath);
        this.gTasksRootUrl = gTaskRootUrl;
    }

    Credential authorize() throws Exception {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(new GoogleClientSecrets.Details().setClientId(gTaskConfig.getClientId())
                .setClientSecret(gTaskConfig.getClientKey()));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GTasksApiService.JSON_FACTORY, clientSecrets,
                Collections.singleton(TasksScopes.TASKS_READONLY)).setDataStoreFactory(
                dataStoreFactory).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver.Builder().setPort(port).build()).authorize("user");
    }

    public Tasks instantiateGapiServiceConsole() {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(this.dataStoreDir);
            // authorization
            Credential cred = authorize();
            return instantiateGapiService(cred.getAccessToken());
        } catch (Exception e) {
            System.out.println("Google Tasks authorization failed:");
            System.out.println(e);
            System.exit(0);
        }
        return null;
    }

    public Tasks instantiateGapiService(String accessToken) throws IOException, GeneralSecurityException {
        // sanitize accessToken
        String accessTokenString = StringUtils.removeStart(accessToken, "Bearer ");
        if (this.tasksService == null ||
                !this.accessToken.equals(accessTokenString)) {
            this.accessToken = accessTokenString;
            GoogleCredential credential = new GoogleCredential().setAccessToken(this.accessToken);
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.tasksService = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .setRootUrl(gTasksRootUrl)
                    .build();
        }
        return this.tasksService;
    }
}
