package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.ServiceProvider;

public interface DistributionMetricsService extends ServiceProvider {

    String NAME = "DistributionMetricsService";

    String[] onMetrics(String serviceName,String[] categories);
}
