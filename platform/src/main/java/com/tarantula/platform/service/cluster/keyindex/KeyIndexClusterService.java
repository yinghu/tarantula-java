package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.KeyIndex;

import java.util.Properties;

public class KeyIndexClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger logger = JDKLogger.getLogger(KeyIndexClusterService.class);
    private NodeEngine nodeEngine;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new KeyIndexServiceProxy(objectName,this.nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public KeyIndex setIfAbsent(String key,KeyIndex pending){
        logger.warn("set if absent ["+key+"]");
        pending.index(key);
        pending.disabled(true);
        return pending;
    }
}
