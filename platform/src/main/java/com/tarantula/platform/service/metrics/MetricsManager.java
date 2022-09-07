package com.tarantula.platform.service.metrics;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.TarantulaContext;

import java.util.concurrent.ConcurrentHashMap;

public class MetricsManager implements SchedulingTask, Serviceable {

    private ConcurrentHashMap<String, Metrics> metricsMap = new ConcurrentHashMap<>();
    private TarantulaContext tarantulaContext;

    public MetricsManager(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return TimeUtil.durationToNextHour();
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        new Thread(()->{
            metricsMap.forEach((k,v)->v.atHourly());
        }).start();
        this.tarantulaContext.schedule(this);
    }


    public Metrics metrics(String name){
        return metricsMap.get(name);
    }

    public void addMetrics(Metrics metrics){
        metricsMap.put(metrics.name(),metrics);
    }
    public void removeMetrics(Metrics metrics){
        metricsMap.remove(metrics.name());
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        metricsMap.forEach((k,v)-> {try{v.shutdown();}catch (Exception ex){}});
    }
}
