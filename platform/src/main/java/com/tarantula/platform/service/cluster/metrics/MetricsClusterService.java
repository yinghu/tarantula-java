package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.Property;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.metrics.MetricsSnapshotRequest;
import com.tarantula.platform.service.metrics.ServiceViewRequest;


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

    public String metricsPayload(String serviceName){
        ServiceViewRequest request = new ServiceViewRequest(nodeEngine.getLocalMember().getUuid());
        ServiceProvider serviceProvider = this.tarantulaContext.serviceProvider(serviceName);
        serviceProvider.updateSummary(request);
        return request.toJson().toString();
    }
    public String metricsSnapshot(String name,String category,String classifier){
        Metrics m = this.tarantulaContext.metrics(name);
        Property[] dat = m.snapshot(category,classifier);
        MetricsSnapshotRequest request = new MetricsSnapshotRequest(nodeEngine.getLocalMember().getUuid(),name,category,classifier);
        request.snapshot(dat);
        return request.toJson().toString();
    }
}
