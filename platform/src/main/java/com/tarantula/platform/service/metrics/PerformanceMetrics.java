package com.tarantula.platform.service.metrics;

import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PerformanceMetrics extends AbstractMetrics {

    public final static String DATA_STORE_COUNT = "dataStoreCount";
    public final static String CLUSTER_INBOUND_MESSAGE_COUNT = "clusterInboundMessageCount";
    public final static String CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterOutboundMessageCount";
    public final static String HTTP_REQUEST_COUNT = "httpRequestCount";
    public final static String UDP_REQUEST_COUNT = "udpRequestCount";


    public void _setup(ServiceContext serviceContext){
        this.name = Metrics.PERFORMANCE;
        this.categories = new String[5];
        this.categories[0] = DATA_STORE_COUNT;
        this.categories[1] = CLUSTER_INBOUND_MESSAGE_COUNT;
        this.categories[2] = CLUSTER_OUTBOUND_MESSAGE_COUNT;
        this.categories[3] = HTTP_REQUEST_COUNT;
        this.categories[4] = UDP_REQUEST_COUNT;
        this.logger = serviceContext.logger(PerformanceMetrics.class);
        this.dataStore = serviceContext.dataStore("metrics_performance",serviceContext.partitionNumber());
    }

}
