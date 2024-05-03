package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class ServiceViewMonitor implements SchedulingTask {

    private TarantulaLogger logger = JDKLogger.getLogger(ServiceViewMonitor.class);

    private final ApplicationContext applicationContext;
    private final ServiceProvider serviceProvider;
    private int timerCountDown;
    public final long timerInternal;

    private final ServiceViewSummary serviceView;

    private final DistributionMetricsService distributionMetricsService;

    public ServiceViewMonitor(ApplicationContext context,ServiceProvider serviceProvider,long timerInternal,int timerCountDown,ServiceViewSummary serviceView){
        this.applicationContext = context;
        this.distributionMetricsService = this.applicationContext.clusterProvider().serviceProvider(DistributionMetricsService.NAME);
        this.serviceProvider = serviceProvider;
        this.timerInternal = timerInternal;
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
            byte[][] ret = distributionMetricsService.onMonitor(serviceProvider.name());
            for(byte[] f  : ret){
                ServiceViewRequest response = ServiceViewRequest.response(f);
                LocalDateTime lastViewed = serviceView.update(response.toJson());
                long seconds = TimeUtil.durationUTCInSeconds(lastViewed,LocalDateTime.now());
                if(seconds>30){
                    logger.warn("Monitor is stopping with last view passed in seconds ["+seconds+"] ");
                    timerCountDown = 0;
                    break;
                }
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
