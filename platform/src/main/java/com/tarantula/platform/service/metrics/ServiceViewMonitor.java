package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ServiceProvider;

public class ServiceViewMonitor implements SchedulingTask {

    private ApplicationContext applicationContext;
    private String serviceName;
    //private long timerInternal;
    private long timerCountDown = 100;
    public final static long timerInternal = 10000;

    private ServiceView serviceView;

    public ServiceViewMonitor(ApplicationContext context,String serviceName,long timerCountDown){
        this.applicationContext = context;
        this.serviceName = serviceName;
        this.timerCountDown = timerCountDown;
        this.serviceView = new ServiceView();
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
        ServiceProvider serviceProvider = applicationContext.serviceProvider(serviceName);
        if(serviceProvider==null) return;
        serviceProvider.updateSummary(serviceView);

    }
}
