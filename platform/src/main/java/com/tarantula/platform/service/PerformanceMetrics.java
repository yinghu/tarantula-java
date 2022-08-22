package com.tarantula.platform.service;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;


public class PerformanceMetrics implements MetricsListener, SchedulingTask {

    public final static String DATA_STORE_COUNT = "dataStoreCount";
    public final static String CLUSTER_INBOUND_MESSAGE_COUNT = "clusterInboundMessageCount";
    public final static String CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterOutboundMessageCount";
    public final static String HTTP_REQUEST_COUNT = "httpRequestCount";
    public final static String UDP_REQUEST_COUNT = "udpRequestCount";


    private TarantulaLogger logger;

    private double count;

    public void setup(ServiceContext serviceContext){
        logger = serviceContext.logger(PerformanceMetrics.class);
        serviceContext.schedule(this);
    }


    @Override
    public void onUpdated(String s, double delta) {
        count += delta;
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
        logger.warn("Total count ->"+count);
        count = 0;
    }
}
