package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ServiceProvider;

public class ServiceViewMonitor implements SchedulingTask {

    private final ApplicationContext applicationContext;
    private final ServiceProvider serviceProvider;
    private int timerCountDown;
    public final static long timerInternal = 1000;

    private final ServiceView serviceView;

    private final DistributionMetricsService distributionMetricsService;

    public ServiceViewMonitor(ApplicationContext context,ServiceProvider serviceProvider,int timerCountDown,ServiceView serviceView){
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
            JsonArray cs = serviceView.toCategoryJson().get("list").getAsJsonArray();
            String[] list = new String[cs.size()];
            for(int i=0;i<cs.size();i++){
                list[i]=cs.get(i).getAsString();
            }
            String[] ret = distributionMetricsService.onMetrics(serviceProvider.name(),list);
            for(String f  : ret){
                applicationContext.log(f, OnLog.WARN);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        serviceProvider.updateSummary(serviceView);
        timerCountDown--;
        if(timerCountDown <= 0){
            serviceView.stop();
            return;
        }
        applicationContext.schedule(this);
    }
}
