package com.tarantula.platform.service.cluster.item;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.TarantulaContext;


import java.util.Properties;


public class ItemClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(ItemClusterService.class);

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
        return new DistributionItemServiceProxy(objName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public boolean register(String gameServiceName,String serviceName, String category,String itemId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider) this.tarantulaContext.serviceProvider(gameServiceName);
        return gameServiceProvider.clusterConfigurationCallback(serviceName).onItemRegistered(category,itemId);
    }
    public boolean release(String gameServiceName,String serviceName, String category,String itemId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider) this.tarantulaContext.serviceProvider(gameServiceName);
        return gameServiceProvider.clusterConfigurationCallback(serviceName).onItemReleased(category,itemId);
    }
}
