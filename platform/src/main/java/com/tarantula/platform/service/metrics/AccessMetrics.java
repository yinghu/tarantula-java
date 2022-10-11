package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class AccessMetrics extends AbstractMetrics{


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.ACCESS;
        this.accessIncluded = true;
        this.accountIncluded = true;
        this.logger = serviceContext.logger(AccessMetrics.class);
        this.dataStore = serviceContext.dataStore("tarantula_access_metrics",serviceContext.partitionNumber());
    }
}
