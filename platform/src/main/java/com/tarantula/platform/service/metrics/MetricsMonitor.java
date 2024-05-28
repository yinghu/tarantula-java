package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceProvider;

import java.util.concurrent.ConcurrentHashMap;

abstract public class MetricsMonitor implements ServiceProvider {

    protected Metrics metrics;
    protected ConcurrentHashMap<String,Double> delta = new ConcurrentHashMap<>();


    @Override
    public void start() throws Exception {
        metrics.categories().forEach(cat->{
            delta.put(cat,metrics.statistics().entry(cat).total());
        });
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void registerSummary(Summary summary){
        metrics.categories().forEach(cat->{
            delta.put(cat,metrics.statistics().entry(cat).total());
        });
        metrics.registerSummary(summary);
    }
    @Override
    public void updateSummary(Summary summary){
        metrics.categories().forEach(cat->{
            double current = metrics.statistics().entry(cat).total();
            Double previous = delta.get(cat);
            if(previous != null){
                double gain = current-previous;
                summary.update(cat,gain);
            }
            delta.put(cat,current);
        });
    }
}
