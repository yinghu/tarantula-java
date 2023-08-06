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
import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class KeyIndexServiceProxy  extends AbstractDistributedObject<KeyIndexClusterService> implements DistributionKeyIndexService, DistributedObject {
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
        return DistributionKeyIndexService.NAME;
    }


    public byte[] recover(byte[] key) {
        NodeEngine nodeEngine = getNodeEngine();
        KeyIndexLookupOperation operation = new KeyIndexLookupOperation(key);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionKeyIndexService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult ret = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()-> {
            Future<KeyIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!ret.successful) throw new RuntimeException(ret.exception);
        return (byte[]) ret.result;
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
