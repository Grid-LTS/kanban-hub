package com.github.gridlts.kanbanhub.taskw.service;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * conversion between date class and date strings provided by the various APIs
 * Google Tasks and Taskwarrior use UTC
 */
public class DateTimeHelper {

    final public static String TASKW_DATE_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

    public static String convertZoneDateTimeToTaskwDate(ZonedDateTime zonedDateTime) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(TASKW_DATE_PATTERN).withZone(ZoneId.of("UTC"));
        return zonedDateTime.format(f);
    }
}