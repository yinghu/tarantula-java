package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class AccessMetrics extends AbstractMetrics{


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.ACCESS;
        this.accessIncluded = true;
        this.accountIncluded = true;
        this.logger = JDKLogger.getLogger(AccessMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,"tarantula_access_metrics");
    }
}
