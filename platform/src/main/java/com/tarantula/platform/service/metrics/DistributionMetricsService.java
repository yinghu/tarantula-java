package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.ServiceProvider;

import java.time.LocalDateTime;

public interface DistributionMetricsService extends ServiceProvider {

    String NAME = "DistributionMetricsService";

    byte[][] onMonitor(String serviceName);

    String[] onMetrics(String name,String category,String classifier);

    String[] onMetricsArchive(String name, String category, String classifier, LocalDateTime end);
}
