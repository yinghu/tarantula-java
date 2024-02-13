package com.icodesoftware.protocol.statistics;

import java.time.LocalDateTime;

public class StatisticsUtil {


    public static boolean validateHourly(LocalDateTime lastUpdate,LocalDateTime currentUpdate){
        return validYear(lastUpdate,currentUpdate) && validDay(lastUpdate,currentUpdate) && validHour(lastUpdate,currentUpdate);
    }

    public static boolean validateDaily(LocalDateTime lastUpdate,LocalDateTime currentUpdate){
        return validYear(lastUpdate,currentUpdate) && validDay(lastUpdate,currentUpdate);
    }

    public static boolean validateWeekly(LocalDateTime lastUpdate,LocalDateTime currentUpdate){
        return validYear(lastUpdate,currentUpdate) && validWeek(lastUpdate,currentUpdate);
    }

    public static boolean validateMonthly(LocalDateTime lastUpdate,LocalDateTime currentUpdate){
        return validYear(lastUpdate,currentUpdate) && validMonth(lastUpdate,currentUpdate);
    }

    public static boolean validateYearly(LocalDateTime lastUpdate,LocalDateTime currentUpdate){
        return validYear(lastUpdate,currentUpdate);
    }

    private static boolean validYear(LocalDateTime lastDateTime,LocalDateTime currentUpdate){
        return lastDateTime.getYear() == currentUpdate.getYear();
    }

    private static boolean validMonth(LocalDateTime lastDateTime,LocalDateTime currentUpdate){
        return lastDateTime.getMonth().getValue() == currentUpdate.getMonth().getValue();
    }

    private static boolean validWeek(LocalDateTime lastDateTime,LocalDateTime currentUpdate){
        int lastNextMonday = lastDateTime.plusDays(8-lastDateTime.getDayOfWeek().getValue()).getDayOfYear();
        int nextMonday = currentUpdate.plusDays(8-currentUpdate.getDayOfWeek().getValue()).getDayOfYear();
        return lastNextMonday == nextMonday;
    }

    private static boolean validDay(LocalDateTime lastDateTime,LocalDateTime currentUpdate){
        return lastDateTime.getDayOfYear() == currentUpdate.getDayOfYear();
    }

    private static boolean validHour(LocalDateTime lastDateTime,LocalDateTime currentUpdate){
        return lastDateTime.getHour() == currentUpdate.getHour();
    }
}
