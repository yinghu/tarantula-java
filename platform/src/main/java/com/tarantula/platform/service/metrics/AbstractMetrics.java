package com.tarantula.platform.service.metrics;

import com.icodesoftware.DataStore;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.Statistics;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.statistics.StatsDelta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


abstract public class AbstractMetrics implements Metrics, SchedulingTask, Serviceable {


    private ConcurrentHashMap<String, StatsDelta> pendingUpdats;
    private AtomicBoolean atMidnight;

    protected long pendingUpdateInterval = 100000;
    protected DataStore dataStore;
    private SystemStatistics statistics;
    private ServiceContext serviceContext;
    protected String[] categories = new String[0];
    protected TarantulaLogger logger;
    protected String name;
    public String name(){
        return name;
    }

    public void setup(ServiceContext serviceContext){
        _setup(serviceContext);
        this.serviceContext = serviceContext;
        atMidnight = new AtomicBoolean(false);
        this.pendingUpdats = new ConcurrentHashMap<>();
        for(String category : categories){
            this.pendingUpdats.put(category,new StatsDelta(category,0));
        }
        String nodeId = serviceContext.nodeId();
        String dayAndYear = labelDayAndYear();
        this.statistics = new SystemStatistics();
        this.statistics.distributionKey(nodeId);
        this.statistics.label(dayAndYear);
        this.statistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(statistics,true);
        serviceContext.schedule(this);
    }


    @Override
    public void onUpdated(String s, double delta) {
        pendingUpdats.compute(s,(k,v)->{
            if(v==null || delta<=0) return v;//ignore
            v.value += delta;
            return v;
        });
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return pendingUpdateInterval;
    }

    @Override
    public void run() {
        if(!atMidnight.get()) return;
        ArrayList<StatsDelta> pendings = new ArrayList<>();
        for(String category : categories){
            pendingUpdats.compute(category,(k,v)->{
                pendings.add(v.reset());
                return v;
            });
        }
        pendings.forEach(p->{
            try {
                if(p.value>0) statistics.entry(p.name).update(p.value).update();
            }catch (Exception ex){
                //ignore
            }
        });
        pendings.clear();
    }

    @Override
    public Statistics statistics() {
        return statistics;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.run();
    }

    public void atMidnight(){
        atMidnight.set(true);
        SystemStatistics next = new SystemStatistics();
        next.distributionKey(this.serviceContext.nodeId());
        next.label(labelDayAndYear());
        next.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(next,true);
        this.run();
        statistics = next;
        atMidnight.set(false);
    }

    private String labelDayAndYear(){
        LocalDateTime today = LocalDateTime.now();
        return today.getYear()+"_"+today.getDayOfYear();
    }
    /**
     * set data store
     * set categories
     * set logger
     * set pendingUpdateInterval
     * */
    abstract void _setup(ServiceContext serviceContext);

}
