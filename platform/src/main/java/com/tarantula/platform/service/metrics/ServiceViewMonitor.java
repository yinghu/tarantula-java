package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.JsonUtil;

public class ServiceViewMonitor implements SchedulingTask {

    private final ApplicationContext applicationContext;
    private final ServiceProvider serviceProvider;
    private int timerCountDown;
    public final static long timerInternal = 1000;

    private final ServiceViewSummary serviceView;

    private final DistributionMetricsService distributionMetricsService;

    public ServiceViewMonitor(ApplicationContext context,ServiceProvider serviceProvider,int timerCountDown,ServiceViewSummary serviceView){
        this.applicationContext = context;
        this.distributionMetricsService = this.applicationContext.clusterProvider().serviceProvider(DistributionMetricsService.NAME);
        this.serviceProvider = serviceProvider;
        this.timerCountDown = timerCountDown;
        this.serviceView = serviceView;
        this.serviceProvider.registerSummary(this.serviceView);
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
            String[] ret = distributionMetricsService.onMetrics(serviceProvider.name());
            for(String f  : ret){
                serviceView.update(JsonUtil.parse(f));
            }
            timerCountDown--;
            if(timerCountDown <= 0){
                serviceView.stop();
                return;
            }
            applicationContext.schedule(this);
        }catch (Exception ex){
            this.applicationContext.log(serviceView.name()+" stopped",ex,OnLog.ERROR);
            serviceView.stop();
        }
    }
}
