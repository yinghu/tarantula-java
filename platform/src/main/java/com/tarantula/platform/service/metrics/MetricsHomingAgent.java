package com.tarantula.platform.service.metrics;

import com.icodesoftware.Statistics;

import java.util.List;

public interface MetricsHomingAgent {
    void onMetrics(String name, List<Statistics.Entry> updated);
}
