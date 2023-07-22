package com.tarantula.platform.service.metrics;


import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class DeploymentMetrics extends AbstractMetrics{

    //DEPLOYMENT CATEGORY

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.DEPLOYMENT;
        this.deploymentIncluded = true;
        this.logger = JDKLogger.getLogger(DeploymentMetrics.class);
        this.dataStore = serviceContext.dataStore("tarantula_deployment_metrics",serviceContext.node().partitionNumber());
    }
}
