package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class DataStoreMetrics extends AbstractMetrics{


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.DATA_STORE;
        this.accessIncluded = true;
        this.accountIncluded = true;
        this.logger = JDKLogger.getLogger(DataStoreMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_data_store_metrics");
    }
}
