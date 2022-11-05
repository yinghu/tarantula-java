package com.tarantula.platform.service.metrics;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.TarantulaContext;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsManager implements SchedulingTask, Serviceable {

    //private TarantulaLogger logger = JDKLogger.getLogger(MetricsManager.class);

    private ConcurrentHashMap<String, Metrics> metricsMap = new ConcurrentHashMap<>();
    private TarantulaContext tarantulaContext;
    private int nextHour;

    public MetricsManager(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        LocalDateTime cur = LocalDateTime.now();
        LocalDateTime to50 = cur.plusDays(nextHour==0?1:0).toLocalDate().atTime(nextHour,50,0,0);
        //logger.warn("Next run at ["+to50+"]");
        long nextRun = TimeUtil.durationUTCMilliseconds(cur,to50);
        if(nextRun>0) return nextRun;
        return 100;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        metricsMap.forEach((k,v)->v.atHourly());
        nextHour = LocalDateTime.now().plusHours(1).getHour();
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
        this.nextHour = LocalDateTime.now().getHour();
        this.tarantulaContext.schedule(this);
    }
    @Override
    public void shutdown() throws Exception {
        metricsMap.forEach((k,v)-> {try{v.shutdown();}catch (Exception ex){}});
    }

}
