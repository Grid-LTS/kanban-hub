package com.github.gridlts.khapi.gtasks.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

    public static LocalDate convertGoogleTimeToDate(com.google.api.client.util.DateTime dateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return LocalDate.parse(dateTime.toStringRfc3339(), f);
    }
}
