package com.tarantula.platform.service.cluster;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;;
import com.tarantula.Descriptor;
import com.tarantula.OnView;
import com.tarantula.admin.GameCluster;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.Batch;
import com.tarantula.platform.service.DeployService;

import java.util.concurrent.Future;

public class DeployServiceProxy extends AbstractDistributedObject<ClusterDeployService> implements DeployService{

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
    public Batch query(int registryId,String[] params){
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

    public void recover(String destination,String registerId,boolean fullBackup){
        NodeEngine nodeEngine = getNodeEngine();
        DeployServiceRecoverOperation operation = new DeployServiceRecoverOperation(destination,registerId,fullBackup);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(); //retry if timeout
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
    public boolean addView(OnView view){
        NodeEngine nodeEngine = getNodeEngine();
        AddViewOperation operation = new AddViewOperation(view);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public boolean resetModule(String lobbyId,Descriptor descriptor){
        NodeEngine nodeEngine = getNodeEngine();
        ResetModuleOperation operation = new ResetModuleOperation(lobbyId,descriptor);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public GameCluster createGameCluster(String owner,String name,String plan){
        NodeEngine nodeEngine = getNodeEngine();
        CreateGameClusterOperation operation = new CreateGameClusterOperation(owner,name,plan);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DeployService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<GameCluster> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
}
