package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class KeyIndexServiceProxy  extends AbstractDistributedObject<KeyIndexClusterService> implements KeyIndexService, DistributedObject {
    private static TarantulaLogger logger = JDKLogger.getLogger(KeyIndexServiceProxy.class);
    private final String objectName;
    public KeyIndexServiceProxy(String objectName, NodeEngine nodeEngine, KeyIndexClusterService keyIndexService){
        super(nodeEngine,keyIndexService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public String getServiceName() {
        return KeyIndexService.NAME;
    }


    public KeyIndex lookup(String pending) {
        NodeEngine nodeEngine = getNodeEngine();
        KeyIndexLookupOperation operation = new KeyIndexLookupOperation(pending);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(KeyIndexService.NAME,operation,nodeEngine.getMasterAddress());
        final Future<KeyIndex> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            logger.error("error on lookup",e);
            return null;
        }
    }


    @Override
    public String name() {
        return objectName;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void setup(ServiceContext serviceContext){
        logger.warn("Key index service proxy started");
    }
}
