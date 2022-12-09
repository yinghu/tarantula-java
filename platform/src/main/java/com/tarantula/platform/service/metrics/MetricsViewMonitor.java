package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.JsonUtil;

import java.util.concurrent.ConcurrentHashMap;


public class MetricsViewMonitor implements SchedulingTask {

    private final ApplicationContext applicationContext;

    public final static long timerInternal = 5000;

    private final DistributionMetricsService distributionMetricsService;

    private final ConcurrentHashMap<String,MetricsSnapshotRequest> listeners;

    public MetricsViewMonitor(ApplicationContext context){
        this.applicationContext = context;
        this.listeners = new ConcurrentHashMap<>();
        this.distributionMetricsService = this.applicationContext.clusterProvider().serviceProvider(DistributionMetricsService.NAME);
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return this.timerInternal;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        try{
           listeners.forEach((k,r)->{
               r.reset();
               String[] ret = distributionMetricsService.onMetrics(r.name,r.category,r.classifier);
               for(String f  : ret) {
                   JsonObject m = JsonUtil.parse(f);
                   r.snapshot(m);
               }
               r.loaded();
           });
        }catch (Exception ex){

        }
        finally {
            this.applicationContext.schedule(this);
        }
    }

    public void register(MetricsSnapshotRequest metricsSnapshotRequest){
        this.listeners.put(metricsSnapshotRequest.toString(),metricsSnapshotRequest);
    }
    public JsonObject snapshot(String name,String category,String classifier){
        return listeners.get(toKey(name,category,classifier)).toJson();
    }
    public JsonObject archive(String name,String category,String classifier){
        return listeners.get(toKey(name,category,classifier)).toJson();
    }
    private String toKey(String name,String category,String classifier){
        return name+"_"+category+"_"+classifier;
    }

}
