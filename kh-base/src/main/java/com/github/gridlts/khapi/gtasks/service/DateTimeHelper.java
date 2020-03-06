package com.github.gridlts.khapi.gtasks.service;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * conversion between date class and date strings provided by the various APIs
 * Google Tasks and Taskwarrior use UTC
 */
public class DateTimeHelper {

    final static String RFC_3339_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    final static DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter.ofPattern(RFC_3339_PATTERN)
            .withZone(ZoneId.of("UTC"));
    final public static String TASKW_DATE_PATTERN = "yyyyMMdd'T'HHmmss'Z'";


    public static LocalDate convertGoogleTimeToDate(com.google.api.client.util.DateTime dateTime) {

        return LocalDate.parse(dateTime.toStringRfc3339(), RFC_3339_FORMATTER);
    }

    public static ZonedDateTime convertGoogleTimeToZonedDateTime(com.google.api.client.util.DateTime dateTime) {
        return ZonedDateTime.parse(dateTime.toStringRfc3339(), RFC_3339_FORMATTER);
    }

    public static String convertZoneDateTimeToRFC3339Timestamp(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(RFC_3339_FORMATTER);
    }

    public static String convertZoneDateTimeToTaskwDate(ZonedDateTime zonedDateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(TASKW_DATE_PATTERN).withZone(ZoneId.of("UTC"));
        return zonedDateTime.format(f);
    }

    public static ZonedDateTime convertUnixTimestampToZonedDateTime(Long unixTime) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.of("UTC"));
    }

    public static ZonedDateTime getOldEnoughDate() {
        String date = "1990-01-01";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, f);
        return localDate.atStartOfDay(ZoneId.of("UTC"));
    }
}
