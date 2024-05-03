package com.tarantula.platform.service.metrics;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;

public class DataStoreMonitor implements ServiceProvider {

    private TarantulaLogger logger = JDKLogger.getLogger(DataStoreMonitor.class);

    public static final String NAME = "DataStoreMonitor";

    private ServiceContext serviceContext;
    @Override
    public String name() {
        return NAME;
    }

    public DataStoreMonitor(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        logger.warn("starting data store monitor");
    }
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void registerSummary(Summary summary){
        Metrics metrics = serviceContext.metrics(DataStoreMetrics.DATA_STORE);
        metrics.registerSummary(summary);
    }
    @Override
    public void updateSummary(Summary summary){
        Metrics metrics = serviceContext.metrics(DataStoreMetrics.DATA_STORE);
        metrics.updateSummary(summary);
    }
}
