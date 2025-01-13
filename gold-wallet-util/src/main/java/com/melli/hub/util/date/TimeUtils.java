package com.melli.hub.util.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * utility class that uses java.time.* instead of java.util.Date
 */
public class TimeUtils {

    private TimeUtils() {
    }

    public static String gregorianDateTimeAsString(String pattern) {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }
}
