package com.icodesoftware.util;

import java.time.*;

public class TimeUtil {
    public static long toUTCMilliseconds(LocalDateTime dateTime){
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    public static LocalDateTime fromUTCMilliseconds(long milliseconds){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneOffset.UTC);
    }
    public static long durationUTCMilliseconds(LocalDateTime start,LocalDateTime end){
        return Duration.between(start,end).toMillis();
    }
    public static long durationUTCInSeconds(LocalDateTime start,LocalDateTime end){
        return Duration.between(start,end).toMillis()/1000;
    }
    public static LocalDateTime midnight(){
        LocalTime mid = LocalTime.MIDNIGHT;
        LocalDate date = LocalDate.now();
        return LocalDateTime.of(date.plusDays(1),mid);
    }
    public static long toMidnight(){
        LocalTime mid = LocalTime.MIDNIGHT;
        LocalDate date = LocalDate.now();
        LocalDateTime end = LocalDateTime.of(date.plusDays(1),mid);
        return Duration.between(LocalDateTime.now(),end).toMillis();
    }
    public static LocalDateTime toMidnight(String dateStr){
        return LocalDateTime.of(LocalDate.parse(dateStr),LocalTime.MIDNIGHT);
    }
    public static boolean expired(LocalDateTime dateTime){
        return dateTime.isBefore(LocalDateTime.now());
    }
}
