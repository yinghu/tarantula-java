package com.icodesoftware.service;

import com.icodesoftware.Statistics;

public interface Metrics extends Serviceable,MetricsListener{

    String PERFORMANCE = "performance";

    void setup(ServiceContext serviceContext);

    Statistics statistics();

    void atMidnight();
}
