package com.github.gridlts.kanbanhub.gtasks;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * conversion between date class and date strings provided by the Google Tasks APIs
 */
public class DateTimeHelper {

    final static String RFC_3339_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    final static DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter.ofPattern(RFC_3339_PATTERN)
            .withZone(ZoneId.of("UTC"));

    public static LocalDate convertGoogleTimeToDate(com.google.api.client.util.DateTime dateTime) {

        return LocalDate.parse(dateTime.toStringRfc3339(), RFC_3339_FORMATTER);
    }

    public static ZonedDateTime convertGoogleTimeToZonedDateTime(com.google.api.client.util.DateTime dateTime) {
        return ZonedDateTime.parse(dateTime.toStringRfc3339(), RFC_3339_FORMATTER);
    }

    public static String convertZoneDateTimeToRFC3339Timestamp(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(RFC_3339_FORMATTER);
    }
}
