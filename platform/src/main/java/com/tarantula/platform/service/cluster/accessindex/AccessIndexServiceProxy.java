package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;

import com.tarantula.platform.TarantulaContext;

import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AccessIndexServiceProxy extends AbstractDistributedObject<AccessIndexClusterService> implements AccessIndexService,DistributionAccessIndexViewer, DistributedObject {

    private TarantulaLogger logger = JDKLogger.getLogger(AccessIndexServiceProxy.class);
    private final String objectName;//unique proxy name

    private MetricsListener metricsListener;


    public AccessIndexServiceProxy(String objectName, NodeEngine nodeEngine,AccessIndexClusterService accessIndexService){
        super(nodeEngine,accessIndexService);
        this.objectName = objectName;
        this.metricsListener = (k,v)->{};
    }

    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return AccessIndexService.NAME;
    }

    @Override
    public AccessIndex set(String accessKey,int referenceId){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetOperation operation = new AccessIndexSetOperation(accessKey,referenceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<AccessIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
        return (AccessIndex)callResult.result;
    }
    @Override
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetIfAbsentOperation operation = new AccessIndexSetIfAbsentOperation(accessKey,referenceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<AccessIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
        return (AccessIndex)callResult.result;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        //this.serviceContext = serviceContext;
    }
    public void waitForData(){}
    @Override
    public AccessIndex get(String accessKey) {
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexGetOperation operation = new AccessIndexGetOperation(accessKey);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<AccessIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!callResult.successful){

            throw new RuntimeException(callResult.exception);
        }
        return (AccessIndex)callResult.result;
    }

    public boolean onEnable(){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            AccessIndexServiceUpdateOperation operation = new AccessIndexServiceUpdateOperation(true);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(callResult.successful) expected--;
        }
        return expected==0;
    }
    public boolean onDisable(){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            AccessIndexServiceUpdateOperation operation = new AccessIndexServiceUpdateOperation(false);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(callResult.successful) expected--;
        }
        return expected==0;
    }


    public byte[] onRecover(byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        byte[] ret = null;
        AccessIndexRecoverOperation operation = new AccessIndexRecoverOperation(key);
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        for(Member member : members){
            if(member.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,member.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(callResult.successful && callResult.result != null){
                ret = (byte[])callResult.result;
                break;
            }
        }
        return ret;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public String name() {
        return this.objectName;
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }


    //DistributionAccessIndexViewer methods
    @Override
    public byte[] load(String source, byte[] key, ClusterProvider.Node node) {
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexLoadOperation operation = new AccessIndexLoadOperation(source,key);
        Member m = nodeEngine.getClusterService().getMember(node.memberId());
        if(m==null) return null;
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        return callResult.successful?(byte[])callResult.result:null;
    }
}
