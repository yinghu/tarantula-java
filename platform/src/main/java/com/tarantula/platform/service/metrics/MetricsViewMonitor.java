package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.JsonUtil;

import java.util.concurrent.ConcurrentHashMap;


public class MetricsViewMonitor implements SchedulingTask {

    private final ApplicationContext applicationContext;

    public final long timerInternal;

    private final DistributionMetricsService distributionMetricsService;

    private final ConcurrentHashMap<String,MetricsSnapshotRequest> listeners;

    public MetricsViewMonitor(ApplicationContext context,long timerInternal){
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
               String[] ret = r.archived ?
                       distributionMetricsService.onMetricsArchive(r.name,r.category,r.classifier,r.endTime)
                       : distributionMetricsService.onMetrics(r.name,r.category,r.classifier);
               for(String f  : ret) {
                   JsonObject m = JsonUtil.parse(f);
                   r.snapshot(m);
               }
               r.loaded();
           });
        }catch (Exception ex){
            ex.printStackTrace();
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
