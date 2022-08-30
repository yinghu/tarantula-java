package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DeployService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DeployServiceProxy extends AbstractDistributedObject<ClusterDeployService> implements DeployService {

    private final String objectName;
    private static TarantulaLogger logger = JDKLogger.getLogger(DeployServiceProxy.class);

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

    public void onCreateGameCluster(String gameClusterId){
        NodeEngine nodeEngine = getNodeEngine();
        OnCreateGameClusterOperation operation = new OnCreateGameClusterOperation(gameClusterId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
    }

    public boolean addLobby(Descriptor lobby,String publishingId){
        NodeEngine nodeEngine = getNodeEngine();
        AddLobbyOperation operation = new AddLobbyOperation(lobby,publishingId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
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
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean updateView(OnView onView){
        NodeEngine nodeEngine = getNodeEngine();
        UpdateViewOperation operation = new UpdateViewOperation(onView);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }

    public boolean enableLobby(String typeId){
        NodeEngine nodeEngine = getNodeEngine();
        EnableLobbyOperation operation = new EnableLobbyOperation(typeId,true);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public boolean disableLobby(String typeId){
        NodeEngine nodeEngine = getNodeEngine();
        EnableLobbyOperation operation = new EnableLobbyOperation(typeId,false);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
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
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public boolean onEnableGameCluster(String gamaClusterId){
        NodeEngine nodeEngine = getNodeEngine();
        EnableGameClusterOperation operation = new EnableGameClusterOperation(gamaClusterId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean onDisableGameCluster(String gamaClusterId){
        NodeEngine nodeEngine = getNodeEngine();
        DisableGameClusterOperation operation = new DisableGameClusterOperation(gamaClusterId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public boolean startGameService(String gameClusterKey){
        NodeEngine nodeEngine = getNodeEngine();
        StartGameServiceOperation operation = new StartGameServiceOperation(gameClusterKey);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("startGameService error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean onLaunchGameCluster(String gameClusterKey){
        NodeEngine nodeEngine = getNodeEngine();
        LaunchGameClusterOperation operation = new LaunchGameClusterOperation(gameClusterKey);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("launchGameCluster error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean onLaunchApplication(String typeId,String applicationId){
        NodeEngine nodeEngine = getNodeEngine();
        LaunchApplicationOperation operation = new LaunchApplicationOperation(typeId,applicationId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("launchApplication error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean updateResource(String contentUrl,String resourceName){
        NodeEngine nodeEngine = getNodeEngine();
        UpdateResourceOperation operation = new UpdateResourceOperation(contentUrl,resourceName);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("launchUpdateResource error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean deployModule(String contentUrl,String resourceName){
        NodeEngine nodeEngine = getNodeEngine();
        DeployModuleOperation operation = new DeployModuleOperation(contentUrl,resourceName);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("deployModule error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean onShutdownApplication(String typeId,String applicationId){
        NodeEngine nodeEngine = getNodeEngine();
        ShutdownApplicationOperation operation = new ShutdownApplicationOperation(typeId,applicationId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("shutdownApplication error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }
    public boolean onShutdownGameCluster(String gameClusterKey){
        NodeEngine nodeEngine = getNodeEngine();
        ShutdownGameClusterOperation operation = new ShutdownGameClusterOperation(gameClusterKey);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
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
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("shutdownGameCluster error on node->"+m.getAddress(),e);
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
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("shutdownModule error on node->"+m.getAddress(),e);
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
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("updateModule error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
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
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("upload error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
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
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                logger.error("sync error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
        return expected==0;
    }

    public boolean registerChannel(String typeId,Channel channel){
        NodeEngine nodeEngine = getNodeEngine();
        RegisterChannelOperation operation = new RegisterChannelOperation(typeId,channel);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(channel.channelId());
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,partitionId);
        final Future<Boolean> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public void registerConnection(Connection connection){
        NodeEngine nodeEngine = getNodeEngine();
        RegisterConnectionOperation operation = new RegisterConnectionOperation(connection.configurationTypeId(),connection);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("registerConnection error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }
    public void ping(String typeId,String serverId){
        NodeEngine nodeEngine = getNodeEngine();
        PingConnectionOperation operation = new PingConnectionOperation(typeId,serverId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("ping error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }
    public void releaseConnection(Connection connection){
        NodeEngine nodeEngine = getNodeEngine();
        ReleaseConnectionOperation operation = new ReleaseConnectionOperation(connection.configurationTypeId(),connection);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("releaseConnection error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }

    @Override
    public byte[] clusterKey() {
        NodeEngine nodeEngine = getNodeEngine();
        if(nodeEngine.getMasterAddress().equals(nodeEngine.getLocalMember().getAddress())){
            logger.warn("Master node on local node, load key from local disk");
            return null;
        }
        ClusterKeyOperation operation = new ClusterKeyOperation();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public void resetClusterKey(){
        NodeEngine nodeEngine = getNodeEngine();
        ResetClusterKeyOperation operation = new ResetClusterKeyOperation();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("reset cluster key error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }

    public void enablePresenceService(String root,String password,String clusterNameSuffix,String host){
        NodeEngine nodeEngine = getNodeEngine();
        EnablePresenceServiceOperation operation = new EnablePresenceServiceOperation(root,password,clusterNameSuffix,host);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            if(m.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("enable presence service error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }
    public void disablePresenceService(String clusterNameSuffix){
        NodeEngine nodeEngine = getNodeEngine();
        DisablePresenceServiceOperation operation = new DisablePresenceServiceOperation(clusterNameSuffix);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            if(m.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("disable presence service error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }
}
