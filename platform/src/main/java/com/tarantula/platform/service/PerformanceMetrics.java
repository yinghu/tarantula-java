package com.tarantula.platform.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.Statistics;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.statistics.SystemStatistics;

import java.time.LocalDateTime;


public class PerformanceMetrics implements Metrics, SchedulingTask, Serviceable {

    public final static String DATA_STORE_COUNT = "dataStoreCount";
    public final static String CLUSTER_INBOUND_MESSAGE_COUNT = "clusterInboundMessageCount";
    public final static String CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterOutboundMessageCount";
    public final static String HTTP_REQUEST_COUNT = "httpRequestCount";
    public final static String UDP_REQUEST_COUNT = "udpRequestCount";


    private DataStore dataStore;
    private SystemStatistics statistics;
    private ServiceContext serviceContext;

    private TarantulaLogger logger;

    public void setup(ServiceContext serviceContext){
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
        this.statistics.entry(s).update(delta);
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
        try {
            this.dataStore.update(this.statistics);
        }catch (Exception ex){
            //ignore
        }
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
        this.dataStore.update(statistics);
    }

    public void atMidnight(){
        SystemStatistics next = new SystemStatistics();
        next.distributionKey(this.serviceContext.nodeId());
        next.label(labelDayAndYear());
        this.dataStore.createIfAbsent(next,true);
        this.dataStore.update(statistics);
        statistics = next;
    }

    private String labelDayAndYear(){
        LocalDateTime today = LocalDateTime.now();
        return today.getYear()+"_"+today.getDayOfYear();
    }
}
