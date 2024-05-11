package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.ServiceProvider;

import java.time.LocalDateTime;

public interface DistributionMetricsService extends ServiceProvider {

    String NAME = "DistributionMetricsService";

    byte[][] onMonitor(String serviceName);

    byte[][] onMetrics(String name,String category,String classifier);

    byte[][] onMetricsArchive(String name, String category, String classifier, LocalDateTime end);
}
