package com.github.gridlts.kanbanhub.helper;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * conversion between date class and date strings provided by the various APIs
 */
public class DateUtilities {

    final static String RFC_3339_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

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
