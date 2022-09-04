package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PerformanceMetrics extends AbstractMetrics {

    //PERFORMANCE CATEGORY
    public final static String PERFORMANCE_DATA_STORE_COUNT = "dataStoreCount";
    public final static String PERFORMANCE_CLUSTER_INBOUND_MESSAGE_COUNT = "clusterInboundMessageCount";
    public final static String PERFORMANCE_CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterOutboundMessageCount";
    public final static String PERFORMANCE_HTTP_REQUEST_COUNT = "httpRequestCount";
    public final static String PERFORMANCE_UDP_REQUEST_COUNT = "udpRequestCount";


    public void _setup(ServiceContext serviceContext){
        this.name = Metrics.PERFORMANCE;
        registerCategory(PERFORMANCE_DATA_STORE_COUNT);
        registerCategory(PERFORMANCE_CLUSTER_INBOUND_MESSAGE_COUNT);
        registerCategory(PERFORMANCE_CLUSTER_OUTBOUND_MESSAGE_COUNT);
        registerCategory(PERFORMANCE_HTTP_REQUEST_COUNT);
        registerCategory(PERFORMANCE_UDP_REQUEST_COUNT);
        this.logger = serviceContext.logger(PerformanceMetrics.class);
        this.dataStore = serviceContext.dataStore("performance_metrics",serviceContext.partitionNumber());
    }

}
