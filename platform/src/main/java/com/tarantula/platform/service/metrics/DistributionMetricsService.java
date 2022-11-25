package com.tarantula.platform.service.metrics;

public interface DistributionMetricsService {

    String NAME = "DistributionMetricsService";

    String onMetrics(String remote);
}
