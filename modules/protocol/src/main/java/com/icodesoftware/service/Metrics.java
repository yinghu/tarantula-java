package com.icodesoftware.service;

import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;

import java.time.LocalDateTime;
import java.util.List;

public interface Metrics extends ServiceProvider,MetricsListener{

    String PERFORMANCE = "performance";
    String ACCESS = "access";
    String PAYMENT = "payment";
    String DEPLOYMENT = "deployment";
    String SYSTEM ="system";

    List<String> categories();

    Statistics statistics();

    void atHourly();

    Property[] snapshot(String category, String classifier);
    History archive(String category,LocalDateTime end);

    interface History extends Recoverable {
        Property[] hourlyGain();
        double dailyGain();
        double weeklyGain();
        double monthlyGain();
        double yearlyGain();
    }

}
