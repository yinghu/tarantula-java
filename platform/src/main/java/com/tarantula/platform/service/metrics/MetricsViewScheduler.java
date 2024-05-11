package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;

import java.util.concurrent.ConcurrentHashMap;


public class MetricsViewScheduler implements SchedulingTask {

    private final ApplicationContext applicationContext;

    public final long timerInternal;

    private final DistributionMetricsService distributionMetricsService;

    private final ConcurrentHashMap<String,MetricsSnapshotRequest> listeners;

    public MetricsViewScheduler(ApplicationContext context, long timerInternal){
        this.timerInternal = timerInternal;
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
               byte[][] ret = r.archived?distributionMetricsService.onMetricsArchive(r.name,r.category,r.classifier,r.endTime):distributionMetricsService.onMetrics(r.name,r.category,r.classifier);
               for(byte[] f  : ret) {
                   r.fromBinary(f);
               }
               r.loaded();
           });
        }catch (Exception ex){
            applicationContext.log("error on monitor view",ex, OnLog.ERROR);
        }
        finally {
            this.applicationContext.schedule(this);
        }
    }

    public String register(MetricsSnapshotRequest metricsSnapshotRequest){
        String queryId = metricsSnapshotRequest.toString();
        this.listeners.put(queryId,metricsSnapshotRequest);
        return queryId;
    }
    public JsonObject snapshot(String queryId){
        return listeners.get(queryId).toJson();
    }
    public JsonObject archive(String queryId){
        JsonObject ret = listeners.get(queryId).toJson();
        if(ret.get("successful").getAsBoolean()) listeners.remove(queryId);
        return ret;
    }

}
