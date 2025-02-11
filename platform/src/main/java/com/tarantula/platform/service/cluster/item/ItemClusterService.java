package com.tarantula.platform.service.cluster.item;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.PlatformGameServiceProvider;
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
        log.info("Start item cluster service");
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

    public boolean onRegister(String gameServiceName,String serviceName, String category,String itemId){
        //log.warn(gameServiceName+" : "+serviceName+" : "+category+" : "+itemId);
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider) this.tarantulaContext.serviceProvider(gameServiceName);
        return gameServiceProvider.clusterConfigurationCallback(serviceName).onItemRegistered(category,itemId);
    }
    public boolean onRelease(String gameServiceName,String serviceName, String category,String itemId){
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider) this.tarantulaContext.serviceProvider(gameServiceName);
        return gameServiceProvider.clusterConfigurationCallback(serviceName).onItemReleased(category,itemId);
    }
}
