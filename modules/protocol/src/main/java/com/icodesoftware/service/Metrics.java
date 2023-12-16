package com.icodesoftware.service;

import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;

import java.time.LocalDateTime;
import java.util.List;

public interface Metrics extends ServiceProvider,MetricsListener{

    int HOURLY_HISTORY_BUFFER_SIZE = 24;
    String HISTORY_LABEL_PREFIX = "history_";

    int SNAPSHOT_TRACKING_SIZE = 12;
    String SNAPSHOT_LABEL_PREFIX = "snapshot_";


    String PERFORMANCE = "performance";
    String ACCESS = "access";
    String PAYMENT = "payment";
    String DEPLOYMENT = "deployment";

    String SYSTEM ="system";

    List<String> categories();

    Statistics statistics();

    void atHourly();

    Spot[] snapshot(String category, String classifier);
    History archive(String category,LocalDateTime day);
    History archiveWeekly(String category,LocalDateTime day);
    History archiveMonthly(String category,LocalDateTime day);
    History archiveYearly(String category,LocalDateTime day);



    interface History extends Recoverable {
        Spot[] hourlyGain();
        double dailyGain();
        double weeklyGain();
        double monthlyGain();
        double yearlyGain();
    }

    interface Spot extends Recoverable{
        String name();
        double value();

        void name(String name);
        void value(double value);
    }


}
