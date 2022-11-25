package com.tarantula.platform.service.cluster.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.Configuration;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.metrics.ServiceView;


import java.util.Properties;

public class MetricsClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(MetricsClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        tarantulaContext = TarantulaContext.getInstance();
        log.warn("Metrics cluster service started");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new DistributionMetricsServiceProxy(objectName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String objectName) {

    }

    public JsonObject metricsPayload(String serviceName,String categories){
        Configuration configuration = tarantulaContext.configuration("metrics-view-settings");
        ServiceView serviceView = new ServiceView(serviceName,configuration,()->{});
        ServiceProvider serviceProvider = this.tarantulaContext.serviceProvider(serviceName);
        serviceProvider.registerSummary(serviceView);
        serviceProvider.updateSummary(serviceView);
        return serviceView.toMetricsJson(JsonUtil.parseAsJsonArray(categories));
    }
}
