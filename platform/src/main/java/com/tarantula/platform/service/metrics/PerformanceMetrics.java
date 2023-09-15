package com.tarantula.platform.service.metrics;

import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PerformanceMetrics extends AbstractMetrics {


    public void _setup(ServiceContext serviceContext){
        this.name = Metrics.PERFORMANCE;
        this.performanceIncluded = true;
        this.logger = JDKLogger.getLogger(PerformanceMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_performance_metrics");
    }

}
