package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class DeploymentMetrics extends AbstractMetrics{

    //DEPLOYMENT CATEGORY
    public final static String DEPLOYMENT_GAME_CLUSTER_COUNT = "gameClusterCount";
    public final static String DEPLOYMENT_MESSAGE_RECEIVER_COUNT = "messageReceiverCount";
    public final static String DEPLOYMENT_APPLICATION_COUNT = "applicationCount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.DEPLOYMENT;
        this.logger = serviceContext.logger(DeploymentMetrics.class);
        registerCategory(DEPLOYMENT_GAME_CLUSTER_COUNT);
        registerCategory(DEPLOYMENT_MESSAGE_RECEIVER_COUNT);
        registerCategory(DEPLOYMENT_APPLICATION_COUNT);
        this.dataStore = serviceContext.dataStore("deployment_metrics",serviceContext.partitionNumber());
    }
}
