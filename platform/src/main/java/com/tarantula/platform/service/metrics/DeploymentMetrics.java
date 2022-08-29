package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class DeploymentMetrics extends AbstractMetrics{

    public final static String GAME_CLUSTER_COUNT = "gameClusterCount";
    public final static String MESSAGE_RECEIVER_COUNT = "messageReceiverCount";
    public final static String APPLICATION_COUNT = "applicationCount";



    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.DEPLOYMENT;
        this.logger = serviceContext.logger(DeploymentMetrics.class);
        this.categories = new String[3];
        this.categories[0] = GAME_CLUSTER_COUNT;
        this.categories[1] = MESSAGE_RECEIVER_COUNT;
        this.categories[2] = APPLICATION_COUNT;
        this.dataStore = serviceContext.dataStore("deployment_metrics",serviceContext.partitionNumber());
    }
}
