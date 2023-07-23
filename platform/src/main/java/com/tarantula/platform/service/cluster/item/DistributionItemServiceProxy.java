package com.tarantula.platform.service.cluster.item;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceEvent;
import com.icodesoftware.service.ServiceEventLogger;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ServiceEventLog;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class DistributionItemServiceProxy extends AbstractDistributedObject<ItemClusterService> implements DistributionItemService {

    private String objectName;
    private ServiceEventLogger serviceEventLogger;

    public DistributionItemServiceProxy(String objectName, NodeEngine nodeEngine, ItemClusterService itemClusterService){
        super(nodeEngine,itemClusterService);
        this.objectName = objectName;
    }
    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionItemService.NAME;
    }

    @Override
    public boolean onRegisterItem(String gameServiceName,String serviceName,String category,String itemId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        boolean ret = true;
        ItemRegisterOperation operation = new ItemRegisterOperation(gameServiceName,serviceName,category,itemId);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionItemService.NAME,operation,m.getAddress());
            final Future<Boolean> future = builder.invoke();
            try {
                boolean flag = future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
                if(!flag){
                    ret = false;
                }
            } catch (Exception e) {
                future.cancel(true);
                ret = false;
                serviceEventLogger.log(new ServiceEventLog("cluster","onRegisterItem",e));
                break; //stop to next node if failed
            }
        }
        return ret;
    }
    @Override
    public boolean onReleaseItem(String gameServiceName,String serviceName,String category,String itemId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        boolean ret = true;
        ItemReleaseOperation operation = new ItemReleaseOperation(gameServiceName,serviceName,category,itemId);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionItemService.NAME,operation,m.getAddress());
            final Future<Boolean> future = builder.invoke();
            try {
                boolean flag = future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
                if(!flag){
                    ret = false;
                }
            } catch (Exception e) {
                future.cancel(true);
                ret = false;
                serviceEventLogger.log(new ServiceEventLog("cluster","onReleaseItem",e));
                break; //stop to next node if failed
            }
        }
        return ret;
    }
    @Override
    public String name() {
        return DistributionItemService.NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void setup(ServiceContext serviceContext){
        this.serviceEventLogger = serviceContext.serviceEventLogger(NAME);
    }
}
