package com.tarantula.platform.service.metrics;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ServiceProvider;

public class ServiceViewMonitor implements SchedulingTask {

    private ApplicationContext applicationContext;
    private String serviceName;
    private int timerCountDown;
    public final static long timerInternal = 1000;

    private final ServiceView serviceView;


    public ServiceViewMonitor(ApplicationContext context,String serviceName,int timerCountDown,ServiceView serviceView){
        this.applicationContext = context;
        this.serviceName = serviceName;
        this.timerCountDown = timerCountDown;
        this.serviceView = serviceView;
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
        if(serviceProvider==null){
            serviceView.stop();
            return;
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
