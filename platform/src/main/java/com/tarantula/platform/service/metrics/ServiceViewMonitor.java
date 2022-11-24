package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ServiceProvider;

public class ServiceViewMonitor implements SchedulingTask {

    private final ApplicationContext applicationContext;
    private final ServiceProvider serviceProvider;
    private int timerCountDown;
    public final static long timerInternal = 1000;

    private final ServiceView serviceView;


    public ServiceViewMonitor(ApplicationContext context,ServiceProvider serviceProvider,int timerCountDown,ServiceView serviceView){
        this.applicationContext = context;
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
        serviceProvider.updateSummary(serviceView);
        timerCountDown--;
        if(timerCountDown <= 0){
            serviceView.stop();
            return;
        }
        applicationContext.schedule(this);
    }
}
