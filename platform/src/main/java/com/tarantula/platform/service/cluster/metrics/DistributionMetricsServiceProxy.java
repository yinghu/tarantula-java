package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.metrics.DistributionMetricsService;

import java.util.Set;
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
    public String[] onMonitor(String serviceName) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        String[] ret = new String[mlist.size()];
        int i = 0;
        for(Member m : mlist){
            ServiceViewOperation serviceViewOperation = new ServiceViewOperation(serviceName);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionMetricsService.NAME, serviceViewOperation,m.getAddress());
            final Future<String> future = builder.invoke();
            try {
                ret[i] = future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                ret[i]="{}";
            }
            i++;
        }
        return ret;
    }

    @Override
    public String name() {
        return DistributionMetricsService.NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
