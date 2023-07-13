package com.icodesoftware.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

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
    public static LocalDateTime midnight(LocalDate dateTime){
        LocalTime mid = LocalTime.MIDNIGHT;
        return LocalDateTime.of(dateTime.plusDays(1),mid);
    }
    public static LocalDateTime toMidnight(String dateStr){
        return LocalDateTime.of(LocalDate.parse(dateStr),LocalTime.MIDNIGHT);
    }
    public static boolean expired(LocalDateTime dateTime){
        return dateTime.isBefore(LocalDateTime.now());
    }

    public static LocalDateTime fromString(String format,String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.from(formatter.parse(dateTime));
    }
    public static long durationToNextHour(){
        LocalDateTime _cur = LocalDateTime.now();
        LocalDateTime _end = _cur.plusHours(1).minusSeconds(_cur.getMinute()*60+_cur.getSecond());
        return Duration.between(_cur,_end).toMillis();
    }

    public static long durationToNextHour(LocalDateTime start){
        LocalDateTime _end = start.plusHours(1).minusSeconds(start.getMinute()*60+start.getSecond());
        return Duration.between(start,_end).toMillis();
    }

    public static LocalDateTime toLastMonday(LocalDateTime end){
        return end.minusWeeks(1).plusDays(8-end.getDayOfWeek().getValue());
    }
    public static LocalDateTime toFirstDayOfLastMonth(LocalDateTime end){
        return end.minusMonths(1).withDayOfMonth(1);
    }
    public static LocalDateTime toFirstDayOfLastYear(LocalDateTime end){
        return end.minusYears(1).withDayOfYear(1);
    }
}
