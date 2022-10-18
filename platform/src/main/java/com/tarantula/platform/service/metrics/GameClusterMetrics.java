package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.ServiceContext;

public class GameClusterMetrics extends AbstractMetrics{


    public GameClusterMetrics(String name){
        this.name = name;
    }
    @Override
    void _setup(ServiceContext serviceContext) {
        this.gameIncluded = true;
        this.paymentIncluded = true;
        this.accessIncluded = true;
        this.logger = serviceContext.logger(GameClusterMetrics.class);
        this.dataStore = serviceContext.dataStore(name.replaceAll("-","_")+"_game_cluster_metrics",serviceContext.node().partitionNumber());
    }
}
