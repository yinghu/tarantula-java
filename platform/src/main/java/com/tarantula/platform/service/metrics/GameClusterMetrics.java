package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
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
        this.logger = JDKLogger.getLogger(GameClusterMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,name.replaceAll("-","_")+"_game_cluster_metrics");
    }
}
