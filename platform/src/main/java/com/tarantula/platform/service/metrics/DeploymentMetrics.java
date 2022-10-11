package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class DeploymentMetrics extends AbstractMetrics{

    //DEPLOYMENT CATEGORY

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.DEPLOYMENT;
        this.deploymentIncluded = true;
        this.logger = serviceContext.logger(DeploymentMetrics.class);
        this.dataStore = serviceContext.dataStore("tarantula_deployment_metrics",serviceContext.partitionNumber());
    }
}
