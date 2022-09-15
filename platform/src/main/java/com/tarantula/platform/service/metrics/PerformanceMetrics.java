package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PerformanceMetrics extends AbstractMetrics {


    public void _setup(ServiceContext serviceContext){
        this.name = Metrics.PERFORMANCE;
        this.performanceIncluded = true;
        this.logger = serviceContext.logger(PerformanceMetrics.class);
        this.dataStore = serviceContext.dataStore("performance_metrics",serviceContext.partitionNumber());
    }

}
