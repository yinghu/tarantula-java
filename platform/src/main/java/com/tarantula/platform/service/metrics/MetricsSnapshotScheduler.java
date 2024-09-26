package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class MetricsSnapshotScheduler implements SchedulingTask {

    private int timerCountDown;
    private long timerInternal;

    private MetricsSnapshotRequest metricsSnapshotRequest;
    private ApplicationContext applicationContext;
    private DistributionMetricsService distributionMetricsService;


    public MetricsSnapshotScheduler(ApplicationContext applicationContext,long timerInternal, int timerCountDown,MetricsSnapshotRequest request){
        this.timerInternal = timerInternal;
        this.timerCountDown = timerCountDown;
        this.applicationContext = applicationContext;
        this.metricsSnapshotRequest = request;
        distributionMetricsService = this.applicationContext.clusterProvider().serviceProvider(DistributionMetricsService.NAME);
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return timerInternal;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        try{
            metricsSnapshotRequest.reset();
            byte[][] ret = metricsSnapshotRequest.archived? distributionMetricsService.onMetricsArchive(metricsSnapshotRequest.name,metricsSnapshotRequest.category,metricsSnapshotRequest.classifier,metricsSnapshotRequest.endTime) : distributionMetricsService.onMetrics(metricsSnapshotRequest.name,metricsSnapshotRequest.category,metricsSnapshotRequest.classifier);
            for(byte[] f  : ret){
                metricsSnapshotRequest.fromBinary(f);
                LocalDateTime lastViewed = metricsSnapshotRequest.lastViewed.pop();
                long seconds = TimeUtil.durationUTCInSeconds(lastViewed,LocalDateTime.now());
                if(seconds>30){
                    applicationContext.log("Monitor is stopping with last view passed in seconds ["+seconds+"] ",OnLog.WARN);
                    timerCountDown = 0;
                    break;
                }
            }
            metricsSnapshotRequest.loaded();
            timerCountDown--;
            if(timerCountDown <= 0){
                metricsSnapshotRequest.stop();
                return;
            }
            applicationContext.schedule(this);
        }catch (Exception ex){
            this.applicationContext.log(metricsSnapshotRequest.name()+" stopped",ex, OnLog.ERROR);
            metricsSnapshotRequest.stop();
        }
    }
}
