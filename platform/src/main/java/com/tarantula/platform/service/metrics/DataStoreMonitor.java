package com.tarantula.platform.service.metrics;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;

import com.icodesoftware.service.ServiceContext;


public class DataStoreMonitor extends MetricsMonitor {

    private TarantulaLogger logger = JDKLogger.getLogger(DataStoreMonitor.class);

    public static final String NAME = "DataStoreMonitor";

    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        metrics = serviceContext.metrics(DataStoreMetrics.DATA_STORE);
        logger.warn("Starting data store monitor");
    }
}
