package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.metrics.DistributionMetricsService;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionMetricsServiceProxy extends AbstractDistributedObject<MetricsClusterService> implements DistributionMetricsService {

    private String objectName;


    public DistributionMetricsServiceProxy(String objectName, NodeEngine nodeEngine, MetricsClusterService metricsClusterService){
        super(nodeEngine,metricsClusterService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionMetricsService.NAME;
    }

    @Override
    public String onMetrics(String memberId,String serviceName,String categories) {
        NodeEngine nodeEngine = getNodeEngine();
        Member member = nodeEngine.getClusterService().getMember(memberId);
        ServiceViewOperation serviceViewOperation = new ServiceViewOperation(serviceName,categories);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionMetricsService.NAME, serviceViewOperation,member.getAddress());
        final Future<String> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return "{}";
        }
    }
}
