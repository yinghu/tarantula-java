package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.ServiceContext;

public class GameClusterMetrics extends AbstractMetrics{

    public final static String PLAY_COUNT = "playCount";

    public GameClusterMetrics(String name){
        this.name = name;
    }
    @Override
    void _setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(GameClusterMetrics.class);
        this.categories = new String[1];
        this.categories[0] = PLAY_COUNT;
        this.dataStore = serviceContext.dataStore(name.replaceAll("-","_")+"_game_cluster_metrics",serviceContext.partitionNumber());
    }
}
