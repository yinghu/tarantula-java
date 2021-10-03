package com.tarantula.platform.service.cluster.achievement;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.achievement.DistributionAchievementService;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.cluster.item.ItemClusterService;
import com.tarantula.platform.service.cluster.item.ItemRegisterOperation;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class DistributionAchievementServiceProxy extends AbstractDistributedObject<AchievementClusterService> implements DistributionAchievementService {

    private String objectName;

    public DistributionAchievementServiceProxy(String objectName, NodeEngine nodeEngine, AchievementClusterService itemClusterService){
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
    public boolean register(String serviceName,String category,String itemId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        boolean ret = true;
        AchievementRegisterOperation operation = new AchievementRegisterOperation(serviceName,category,itemId);
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
                //goes to next node if failed
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
}
