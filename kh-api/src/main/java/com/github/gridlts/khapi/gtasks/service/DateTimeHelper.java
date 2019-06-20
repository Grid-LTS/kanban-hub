package com.github.gridlts.khapi.gtasks.service;

import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateKeyDeserializer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateTimeHelper {

    final static String RFC_3339_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static LocalDate convertGoogleTimeToDate(com.google.api.client.util.DateTime dateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(RFC_3339_PATTERN).withZone(ZoneId.of("UTC"));
        return LocalDate.parse(dateTime.toStringRfc3339(), f);
    }

    public static ZonedDateTime convertGoogleTimeToZonedDateTime(com.google.api.client.util.DateTime dateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(RFC_3339_PATTERN).withZone(ZoneId.of("UTC"));
        return ZonedDateTime.parse(dateTime.toStringRfc3339(), f);
    }

    public static String convertZoneDateTimeToRFC3339Timestamp(ZonedDateTime zonedDateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(RFC_3339_PATTERN);
        return zonedDateTime.format(f);
    }

    public static  ZonedDateTime convertUnixTimestampToZonedDateTime(Long unixTime) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.of("UTC"));
    }


    public static ZonedDateTime getOldEnoughDate() {
        String date = "1990-01-01";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate  = LocalDate.parse(date, f);
        return localDate.atStartOfDay(ZoneId.of("UTC"));
    }
}
