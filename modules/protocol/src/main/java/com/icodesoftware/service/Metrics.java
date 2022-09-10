package com.icodesoftware.service;

import com.icodesoftware.Property;
import com.icodesoftware.Statistics;

import java.time.LocalDateTime;
import java.util.List;

public interface Metrics extends Serviceable,MetricsListener{

    String PERFORMANCE = "performance";
    String ACCESS = "access";
    String PAYMENT = "payment";
    String DEPLOYMENT = "deployment";
    String SYSTEM ="system";

    String name();
    List<String> categories();

    void setup(ServiceContext serviceContext);

    Statistics statistics();

    void atHourly();

    Property[] snapshot(String category, String classifier);
    Property[] history(String category, String classifier, LocalDateTime start,LocalDateTime end);
 }
