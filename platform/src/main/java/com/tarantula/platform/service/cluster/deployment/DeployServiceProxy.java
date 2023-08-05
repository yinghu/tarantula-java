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
import com.icodesoftware.service.ServiceEventLogger;
import com.tarantula.platform.TarantulaContext;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DeployServiceProxy extends AbstractDistributedObject<ClusterDeployService> implements DeployService {

    private final String objectName;
    private static TarantulaLogger logger = JDKLogger.getLogger(DeployServiceProxy.class);

    private ServiceEventLogger serviceEventLogger;
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
        serviceEventLogger = serviceContext.serviceEventLogger();
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


    public boolean onUpdateView(OnView onView){
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


    public boolean onStartGameService(String gameClusterKey){
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
    public boolean onUpdateResource(String contentUrl,String resourceName){
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
    public boolean onDeployModule(String contentUrl,String resourceName){
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
    public boolean onLaunchModule(String typeId){
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
    public boolean onShutdownModule(String typeId){
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
    public boolean onUpdateModule(Descriptor descriptor){
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

    public boolean onUpload(String fileName,byte[] content){
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

    public boolean onUpdateConfigurable(String key){
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


    public void onRegisterConnection(Connection connection){
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
    public void onVerifyConnection(String typeId,String serverId){
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
    public void onStartConnection(Connection connection){
        NodeEngine nodeEngine = getNodeEngine();
        StartConnectionOperation operation = new StartConnectionOperation(connection.configurationTypeId(),connection);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                logger.error("startConnection error on node->"+m.getAddress(),e);
                //goes to next node if failed
            }
        }
    }
    public void onReleaseConnection(Connection connection){
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
    public byte[] onClusterKey() {
        NodeEngine nodeEngine = getNodeEngine();
        if(nodeEngine.getMasterAddress().equals(nodeEngine.getLocalMember().getAddress())){
            logger.warn("Master node on local node, load key from local disk");
            return null;
        }
        ClusterKeyOperation operation = new ClusterKeyOperation();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        byte[] expected = null;
        for(int i=0; i <TarantulaContext.operationRetries; i++){
            try {
                final Future<byte[]> future = builder.invoke();
                expected = future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
                break;
            } catch (Exception e) {
                logger.warn("re-trying ["+i+"]");
                _wait();
            }
        }
        return expected;
    }

    public void onResetClusterKey(){
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

    public void onEnablePresenceService(String root,String password,String clusterNameSuffix,String host){
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
    public void onDisablePresenceService(String clusterNameSuffix){
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

    private void _wait(){
        try {
            Thread.sleep(TarantulaContext.operationRejectInterval);
        }catch (Exception ex){
            logger.error("error on _wait",ex);
        }
    }
}
