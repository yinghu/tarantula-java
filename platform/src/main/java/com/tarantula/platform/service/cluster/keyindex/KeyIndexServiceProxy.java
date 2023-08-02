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

    @Override
    public void set(KeyIndex pending) {
        NodeEngine nodeEngine = getNodeEngine();
        KeyIndexSetOperation operation = new KeyIndexSetOperation(pending);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(pending.key().asString());
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(KeyIndexService.NAME,operation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace();
            //return null;
        }
    }

    public KeyIndex get(String key){
        return null;
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
        logger.warn("Key index service started");
    }
}
