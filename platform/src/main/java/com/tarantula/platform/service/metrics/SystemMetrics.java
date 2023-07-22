package com.tarantula.platform.service.metrics;


import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class SystemMetrics extends AbstractMetrics{


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.SYSTEM;
        this.accessIncluded = true;
        this.gameIncluded = true;
        this.paymentIncluded = true;
        this.logger = JDKLogger.getLogger(SystemMetrics.class);
        this.dataStore = serviceContext.dataStore("tarantula_system_metrics",serviceContext.node().partitionNumber());
    }
}
