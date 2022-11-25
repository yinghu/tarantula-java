package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.platform.service.metrics.DistributionMetricsService;

public class DistributionMetricsServiceProxy extends AbstractDistributedObject<MetricsClusterService> implements DistributionMetricsService {

    private String objectName;

    public DistributionMetricsServiceProxy(String objectName, NodeEngine nodeEngine, MetricsClusterService metricsClusterService){
        super(nodeEngine,metricsClusterService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionMetricsService.NAME;
    }

    @Override
    public String onMetrics(String remote) {
        return null;
    }
}
