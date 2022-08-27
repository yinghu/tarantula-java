package com.icodesoftware.service;

import com.icodesoftware.Statistics;

public interface Metrics extends Serviceable,MetricsListener{

    String PERFORMANCE = "performance";

    String name();

    void setup(ServiceContext serviceContext);

    Statistics statistics();

    void atMidnight();
}
