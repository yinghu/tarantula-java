package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.TarantulaContext;


import java.util.Properties;


public class PresenceClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(PresenceClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start presence cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objName) {
        return new DistributionPresenceServiceProxy(objName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public int onProfileSequence(String gameServiceName,String profileName){
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider) this.tarantulaContext.serviceProvider(gameServiceName);
        return gameServiceProvider.presenceServiceProvider().onProfileSequence(profileName);
    }

}
