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


public class PerformanceMetrics implements Metrics, SchedulingTask, Serviceable {

    public final static String DATA_STORE_COUNT = "dataStoreCount";
    public final static String CLUSTER_INBOUND_MESSAGE_COUNT = "clusterInboundMessageCount";
    public final static String CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterOutboundMessageCount";
    public final static String HTTP_REQUEST_COUNT = "httpRequestCount";
    public final static String UDP_REQUEST_COUNT = "udpRequestCount";


    private ConcurrentHashMap<String, StatsDelta> pendingUpdats;

    private DataStore dataStore;
    private SystemStatistics statistics;
    private ServiceContext serviceContext;

    private AtomicBoolean atMidnight;


    private TarantulaLogger logger;

    public void setup(ServiceContext serviceContext){
        atMidnight = new AtomicBoolean(false);
        this.pendingUpdats = new ConcurrentHashMap<>();
        this.pendingUpdats.put(DATA_STORE_COUNT,new StatsDelta(DATA_STORE_COUNT,0));
        this.pendingUpdats.put(CLUSTER_INBOUND_MESSAGE_COUNT,new StatsDelta(CLUSTER_INBOUND_MESSAGE_COUNT,0));
        this.pendingUpdats.put(CLUSTER_OUTBOUND_MESSAGE_COUNT,new StatsDelta(CLUSTER_OUTBOUND_MESSAGE_COUNT,0));
        this.pendingUpdats.put(HTTP_REQUEST_COUNT,new StatsDelta(HTTP_REQUEST_COUNT,0));
        this.pendingUpdats.put(UDP_REQUEST_COUNT,new StatsDelta(UDP_REQUEST_COUNT,0));
        this.serviceContext = serviceContext;
        this.logger = serviceContext.logger(PerformanceMetrics.class);
        dataStore = serviceContext.dataStore("metrics_performance",serviceContext.partitionNumber());
        String nodeId = serviceContext.nodeId();
        String dayAndYear = labelDayAndYear();
        logger.warn("Performance metrics hooked on ->"+nodeId+">>"+dayAndYear);
        this.statistics = new SystemStatistics();
        statistics.distributionKey(nodeId);
        statistics.label(dayAndYear);
        statistics.dataStore(this.dataStore);
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
        return 10000;
    }

    @Override
    public void run() {
        if(!atMidnight.get()) return;
        ArrayList<StatsDelta> pendings = new ArrayList<>();
        pendingUpdats.compute(DATA_STORE_COUNT,(k,v)->{
            pendings.add(v.reset());
            return v;
        });
        pendingUpdats.compute(CLUSTER_OUTBOUND_MESSAGE_COUNT,(k,v)->{
            pendings.add(v.reset());
            return v;
        });
        pendingUpdats.compute(CLUSTER_INBOUND_MESSAGE_COUNT,(k,v)->{
            pendings.add(v.reset());
            return v;
        });
        pendingUpdats.compute(HTTP_REQUEST_COUNT,(k,v)->{
            pendings.add(v.reset());
            return v;
        });
        pendingUpdats.compute(UDP_REQUEST_COUNT,(k,v)->{
            pendings.add(v.reset());
            return v;
        });
        pendings.forEach(p->{
            try {
                statistics.entry(p.name).update(p.value).update();
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
        logger.warn("Flushing last data on shut down");
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
}
