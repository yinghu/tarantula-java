package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.Metrics;


public class MetricsViewMonitor implements SchedulingTask {

    private final ApplicationContext applicationContext;

    private int timerCountDown;
    public final static long timerInternal = 10000;

    private final DistributionMetricsService distributionMetricsService;
    private final Metrics metrics;
    private final Listener listener;

    public MetricsViewMonitor(ApplicationContext context,int timerCountDown,Metrics metrics,Listener listener){
        this.applicationContext = context;
        this.distributionMetricsService = this.applicationContext.clusterProvider().serviceProvider(DistributionMetricsService.NAME);
        this.timerCountDown = timerCountDown;
        this.metrics = metrics;
        this.listener = listener;
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
            String[] ret = distributionMetricsService.onMetrics(metrics.name(),PerformanceMetrics.PERFORMANCE_DATA_STORE_COUNT, LeaderBoard.HOURLY);
            for(String f  : ret){
                MetricsSnapshotRequest request = MetricsSnapshotRequest.parse(f);
                listener.onSnapshot(request);
            }
            timerCountDown--;
            if(timerCountDown <= 0){
                listener.onStop();
                return;
            }
            applicationContext.schedule(this);
        }catch (Exception ex){
            listener.onStop();
            this.applicationContext.log(metrics.name()+" stopped",ex, OnLog.ERROR);
        }
    }

    public interface Listener{
        void onSnapshot(MetricsSnapshotRequest request);
        void onStop();
    }
}
