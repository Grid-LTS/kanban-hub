package com.github.gridlts.kanbanhub.helper;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * conversion between date class and date strings provided by the various APIs
 */
public class DateUtilities {

    public static final String RFC_3339_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static ZonedDateTime convertUnixTimestampToZonedDateTime(Long unixTime) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.of("UTC"));
    }

    public static ZonedDateTime getOldEnoughDate() {
        String date = "1990-01-01";
        LocalDate localDate = LocalDate.parse(date, DATE_PATTERN);
        return localDate.atStartOfDay(ZoneId.of("UTC"));
    }

    public static LocalDate convert(String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(s, formatter);
    }


}
