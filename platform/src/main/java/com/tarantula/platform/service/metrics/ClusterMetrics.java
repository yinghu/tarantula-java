package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class ClusterMetrics extends AbstractMetrics{

    public static final String CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterPublishMessageCount";
    public static final String CLUSTER_INBOUND_MESSAGE_COUNT = "clusterReceiveMessageCount";

    public static final String CLUSTER_REMOTE_CALL_COUNT = "clusterRemoteCallCount";

    public static final String CLUSTER_REMOTE_RETRY_COUNT = "clusterRemoteRetryCount";

    public static final String CLUSTER_REMOTE_CALL_FAILURE_COUNT = "clusterRemoteCallFailureCount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.CLUSTER;
        this.logger = JDKLogger.getLogger(ClusterMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_cluster_metrics");
    }
}
