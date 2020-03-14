package com.github.gridlts.khapi.gtasks.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GTasksApiService {

    public static final String APPLICATION_NAME = "Kaban Hub Client";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static String accessToken = "";
    private static Tasks tasksService;

    public static Tasks instantiateGapiService(String accessToken) throws IOException, GeneralSecurityException {
        if (GTasksApiService.tasksService == null ||
                !GTasksApiService.accessToken.equals(accessToken.substring(7))) {
            GTasksApiService.accessToken = accessToken.substring(7);
            GoogleCredential credential = new GoogleCredential().setAccessToken(GTasksApiService.accessToken);
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GTasksApiService.tasksService = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return GTasksApiService.tasksService;
    }
}
