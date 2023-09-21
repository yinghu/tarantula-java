package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class KeyIndexServiceProxy  extends AbstractDistributedObject<KeyIndexClusterService> implements DistributionKeyIndexService, DistributedObject {
    private static TarantulaLogger logger = JDKLogger.getLogger(KeyIndexServiceProxy.class);
    private final String objectName;
    private ServiceContext serviceContext;
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


    public byte[] recover(String source,byte[] key) {
        NodeEngine nodeEngine = getNodeEngine();
        if(nodeEngine.getMasterAddress().equals(nodeEngine.getLocalMember().getAddress())){
            return null;
        }
        KeyIndexLookupOperation operation = new KeyIndexLookupOperation(source,key);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionKeyIndexService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult ret = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()-> {
            Future<KeyIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!ret.successful) throw new RuntimeException(ret.exception);
        return (byte[]) ret.result;
    }

    public boolean startSync(String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        KeyIndexSyncStartOperation operation = new KeyIndexSyncStartOperation(nodeEngine.getLocalMember().getUuid(),syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionKeyIndexService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult ret = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()-> {
            Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!ret.successful) throw new RuntimeException(ret.exception);
        return (boolean)ret.result;
    }

    public void onSync(int size,byte[][] keys,byte[][] values,String memberId,String source){
        NodeEngine nodeEngine = getNodeEngine();
        Member m = nodeEngine.getClusterService().getMember(memberId);
        KeyIndexSyncOperation operation = new KeyIndexSyncOperation(size,keys,values,source);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionKeyIndexService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult ret = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()-> {
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!ret.successful) throw new RuntimeException(ret.exception);
    }

    public boolean endSync(String memberId,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        Member m = nodeEngine.getClusterService().getMember(memberId);
        KeyIndexSyncEndOperation operation = new KeyIndexSyncEndOperation(syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionKeyIndexService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult ret = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()-> {
            Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!ret.successful) throw new RuntimeException(ret.exception);
        return (boolean)ret.result;
    }

    public void load(String source,byte[] key, DataStoreSummary.View view){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> memberSet = nodeEngine.getClusterService().getMembers();
        KeyIndexLookupOperation operation = new KeyIndexLookupOperation(source,key);
        for(Member m : memberSet){
            if(m.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionKeyIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            });
            if(callResult.successful){
                ClusterProvider.Node node = serviceContext.clusterProvider().summary().node(m.getUuid());
                //view.on(node,key,(byte[]) callResult.result);
            }
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
        this.serviceContext = serviceContext;
    }
}
