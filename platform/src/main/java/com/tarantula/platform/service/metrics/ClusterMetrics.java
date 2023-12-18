package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class ClusterMetrics extends AbstractMetrics{


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.CLUSTER;
        this.accessIncluded = true;
        this.accountIncluded = true;
        this.logger = JDKLogger.getLogger(ClusterMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_cluster_metrics");
    }
}
