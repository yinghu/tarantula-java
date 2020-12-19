package com.tarantula.platform.service.cluster;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;;
import com.icodesoftware.*;
import com.icodesoftware.service.Batch;
import com.icodesoftware.service.DeployService;
import com.icodesoftware.service.ServiceContext;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DeployServiceProxy extends AbstractDistributedObject<ClusterDeployService> implements DeployService {

    private final String objectName;


    public DeployServiceProxy(String objectName, NodeEngine nodeEngine, ClusterDeployService deployServiceService){
        super(nodeEngine,deployServiceService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DeployService.NAME;
    }

    @Override
    public String name() {
        return DeployService.NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {

    }

    @Override
    public void waitForData() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public Batch query(int registryId, String[] params){
        NodeEngine nodeEngine = getNodeEngine();
        DeployServiceQueryOperation operation = new DeployServiceQueryOperation(registryId,params);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Batch> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    @Override
    public Batch query(String batchId,int count) {
        NodeEngine nodeEngine = getNodeEngine();
        DeployServiceGetOperation operation = new DeployServiceGetOperation(batchId,count);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Batch> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean addLobby(Descriptor lobby){
        NodeEngine nodeEngine = getNodeEngine();
        AddLobbyOperation operation = new AddLobbyOperation(lobby);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean addView(OnView onView){
        NodeEngine nodeEngine = getNodeEngine();
        AddViewOperation operation = new AddViewOperation(onView);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public String addApplication(Descriptor application){
        NodeEngine nodeEngine = getNodeEngine();
        AddApplicationOperation operation = new AddApplicationOperation(application);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<String> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean enableLobby(String typeId,boolean enabled){
        NodeEngine nodeEngine = getNodeEngine();
        EnableLobbyOperation operation = new EnableLobbyOperation(typeId,enabled);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public String enableApplication(String applicationId,boolean enabled){
        NodeEngine nodeEngine = getNodeEngine();
        EnableApplicationOperation operation = new EnableApplicationOperation(applicationId,enabled);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<String> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }


    public boolean resetModule(Descriptor descriptor){
        NodeEngine nodeEngine = getNodeEngine();
        ResetModuleOperation operation = new ResetModuleOperation(descriptor);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public <T extends OnAccess> T createGameCluster(String owner, String name){
        NodeEngine nodeEngine = getNodeEngine();
        CreateGameClusterOperation operation = new CreateGameClusterOperation(owner,name);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<T> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean enableGameCluster(String gamaClusterId){
        NodeEngine nodeEngine = getNodeEngine();
        EnableGameClusterOperation operation = new EnableGameClusterOperation(gamaClusterId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean disableGameCluster(String gamaClusterId){
        NodeEngine nodeEngine = getNodeEngine();
        DisableGameClusterOperation operation = new DisableGameClusterOperation(gamaClusterId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public byte[] load(String dataSource,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        LoadOperation operation = new LoadOperation(dataSource,key);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean launchGameCluster(String gameClusterKey){
        NodeEngine nodeEngine = getNodeEngine();
        LaunchGameClusterOperation operation = new LaunchGameClusterOperation(gameClusterKey);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean launchApplication(String typeId,String applicationId){
        NodeEngine nodeEngine = getNodeEngine();
        LaunchApplicationOperation operation = new LaunchApplicationOperation(typeId,applicationId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean shutdownApplication(String typeId,String applicationId){
        NodeEngine nodeEngine = getNodeEngine();
        ShutdownApplicationOperation operation = new ShutdownApplicationOperation(typeId,applicationId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean shutdownGameCluster(String gameClusterKey){
        NodeEngine nodeEngine = getNodeEngine();
        ShutdownGameClusterOperation operation = new ShutdownGameClusterOperation(gameClusterKey);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean launchModule(String typeId){
        NodeEngine nodeEngine = getNodeEngine();
        LaunchModuleOperation operation = new LaunchModuleOperation(typeId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean shutdownModule(String typeId){
        NodeEngine nodeEngine = getNodeEngine();
        ShutdownModuleOperation operation = new ShutdownModuleOperation(typeId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean updateModule(Descriptor descriptor){
        NodeEngine nodeEngine = getNodeEngine();
        UpdateModuleOperation operation = new UpdateModuleOperation(descriptor);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }


    public void addServerPushEvent(Event serverPushEvent){
        NodeEngine nodeEngine = getNodeEngine();
        serverPushEvent.clientId(nodeEngine.getLocalMember().getUuid());
        AddServerPushEventOperation operation = new AddServerPushEventOperation(serverPushEvent);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        //int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                //expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        //return expected==0;
    }
    public boolean addServerPushEvent(String memberId,Event serverPushEvent){
        NodeEngine nodeEngine = getNodeEngine();
        AddServerPushEventOperation operation = new AddServerPushEventOperation(serverPushEvent);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = 1;
        for(Member m :mlist){
            if(m.getUuid().equals(memberId)){
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
                final Future<Void> future = builder.invoke();
                try {
                    future.get(5, TimeUnit.SECONDS);
                    expected--;
                } catch (Exception e) {
                    future.cancel(true);
                    //goes to next node if failed
                }
            }
        }
        return expected==0;
    }
    public void removeServerPushEvent(String serverId){
        NodeEngine nodeEngine = getNodeEngine();
        RemoveServerPushEventOperation operation = new RemoveServerPushEventOperation(serverId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        //int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                //expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        //return expected==0;
    }
    public void ackServerPushEvent(String serverId){
        NodeEngine nodeEngine = getNodeEngine();
        AckServerPushEventOperation operation = new AckServerPushEventOperation(serverId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        //int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                //expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        //return expected==0;
    }

    public void getConnection(String typeId,String lobbyTag,Session session){
        NodeEngine nodeEngine = getNodeEngine();
        GetConnectionOperation operation = new GetConnectionOperation(lobbyTag,session);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(typeId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            //return null;
        }
    }
    public boolean upload(String fileName,byte[] content){
        NodeEngine nodeEngine = getNodeEngine();
        DeployServiceUploadOperation operation = new DeployServiceUploadOperation(fileName,content);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(10, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public void syncServerPushEvent(){
        NodeEngine nodeEngine = getNodeEngine();
        ServerPushEventSyncOperation operation = new ServerPushEventSyncOperation(nodeEngine.getLocalMember().getUuid());
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(5,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean sync(String key){
        NodeEngine nodeEngine = getNodeEngine();
        DataSyncOperation operation = new DataSyncOperation(key);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(10, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
}
