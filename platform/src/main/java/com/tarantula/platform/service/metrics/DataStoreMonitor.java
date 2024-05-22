package com.tarantula.platform.service.metrics;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;

import java.util.concurrent.ConcurrentHashMap;

public class DataStoreMonitor implements ServiceProvider {

    private TarantulaLogger logger = JDKLogger.getLogger(DataStoreMonitor.class);

    public static final String NAME = "DataStoreMonitor";

    private ServiceContext serviceContext;

    private ConcurrentHashMap<String,Double> delta = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return NAME;
    }

    public DataStoreMonitor(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        logger.warn("Starting data store monitor");
    }
    @Override
    public void start() throws Exception {
        Metrics metrics = serviceContext.metrics(DataStoreMetrics.DATA_STORE);
        metrics.categories().forEach(cat->{
            delta.put(cat,metrics.statistics().entry(cat).total());
        });
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void registerSummary(Summary summary){
        Metrics metrics = serviceContext.metrics(DataStoreMetrics.DATA_STORE);
        metrics.categories().forEach(cat->{
            delta.put(cat,metrics.statistics().entry(cat).total());
        });
        metrics.registerSummary(summary);
    }
    @Override
    public void updateSummary(Summary summary){
        Metrics metrics = serviceContext.metrics(DataStoreMetrics.DATA_STORE);
        metrics.categories().forEach(cat->{
            double current = metrics.statistics().entry(cat).total();
            double gain = current-delta.get(cat);
            summary.update(cat,gain);
            delta.replace(cat,current);
        });
    }
}
