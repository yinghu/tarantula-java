package com.tarantula.platform.service.cluster.achievement;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.item.DistributionItemServiceProxy;

import java.util.Properties;


public class AchievementClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AchievementClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start item cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objName) {
        return new DistributionAchievementServiceProxy(objName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public boolean register(String serviceName, String category,String itemId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider) this.tarantulaContext.serviceProvider(serviceName);
        return gameServiceProvider.achievementServiceProvider().onRegister(category,itemId);
    }
}
