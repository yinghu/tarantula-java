package com.tarantula.platform.service.metrics;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.ServiceContext;

public class ClusterMonitor extends MetricsMonitor {

    private TarantulaLogger logger = JDKLogger.getLogger(ClusterMonitor.class);

    public static final String NAME = "ClusterMonitor";

    private ClusterProvider integrationCluster;
    @Override
    public String name() {
        return NAME;
    }

    public void setup(ServiceContext serviceContext){
        metrics = serviceContext.metrics(ClusterMetrics.CLUSTER);
        integrationCluster = serviceContext.clusterProvider();
        logger.warn("Starting cluster monitor");
    }
    @Override
    public void start() throws Exception {
        super.start();
        delta.put(ClusterProvider.PENDING_EVENT_NUMBER,0d);
    }

    @Override
    public void registerSummary(Summary summary){
       super.registerSummary(summary);
       integrationCluster.registerSummary(summary);
    }
    @Override
    public void updateSummary(Summary summary){
        super.updateSummary(summary);
        integrationCluster.updateSummary(summary);
    }
}
