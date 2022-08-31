package com.tarantula.platform.service.cluster.deployment;

import com.google.gson.GsonBuilder;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.spi.*;
import com.icodesoftware.*;

import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.deployment.*;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.*;


public class ClusterDeployService implements ManagedService, RemoteService, MembershipAwareService, MigrationListener {

    private static TarantulaLogger log = JDKLogger.getLogger(ClusterDeployService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int scope;
    private GsonBuilder builder;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.nodeEngine.getPartitionService().addMigrationListener(this);
        this.scope = Integer.parseInt(properties.getProperty("tarantula-scope"));
        tarantulaContext = TarantulaContext.getInstance();
        this.deploymentServiceProvider = this.tarantulaContext.deploymentService();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        new ServiceBootstrap(tarantulaContext._integrationClusterStarted,tarantulaContext._deployServiceStarted,new DeployServiceBootstrap(this),"deploy-service",true).start();
    }
    public void setup(){
        log.info("Clustering deployment service started ["+nodeEngine.getConfig().getGroupConfig().getName()+"] on scope ["+this.scope+"]");
    }
    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("deploy service stopped on scope ["+this.scope+"]");
    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new DeployServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {
    }

    public void onCreateGameCluster(String gameClusterId){
        this.deploymentServiceProvider.distributionCallback().onGameClusterCreated(gameClusterId);
    }
    public boolean addLobby(Descriptor descriptor,String publishingId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.bucketId(),descriptor.typeId());
        if(!ds.createIfAbsent(lobbyTypeIdIndex,false)){
            return false;
        }
        ModuleIndex moduleIndex = new ModuleIndex();
        moduleIndex.distributionKey(publishingId);
        moduleIndex.index(descriptor.typeId());
        ds.create(moduleIndex);
        descriptor.owner(publishingId);
        descriptor.label(LobbyDescriptor.LABEL);
        descriptor.onEdge(true);
        descriptor.resetEnabled(true);
        descriptor.disabled(true);
        ds.create(descriptor);
        lobbyTypeIdIndex.index(descriptor.distributionKey());
        lobbyTypeIdIndex.owner(publishingId);
        ds.update(lobbyTypeIdIndex);
        return descriptor.distributionKey()!=null;
    }
    public boolean enableLobby(String typeId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.bucketId(),typeId);
        if(!ds.load(query)){
            return false;
        }
        LobbyDescriptor lobbyDescriptor = new LobbyDescriptor();
        lobbyDescriptor.distributionKey(query.index());
        if(!ds.load(lobbyDescriptor)||!lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(false);
        ds.update(lobbyDescriptor);
        return true;
    }
    public boolean disableLobby(String typeId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.bucketId(),typeId);
        if(!ds.load(query)){
            return false;
        }
        LobbyDescriptor lobbyDescriptor = new LobbyDescriptor();
        lobbyDescriptor.distributionKey(query.index());
        if(!ds.load(lobbyDescriptor)||lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(true);
        ds.update(lobbyDescriptor);
        return true;
    }


    public void onUpdateView(OnView onView){
        this.deploymentServiceProvider.distributionCallback().onViewUpdated(onView);
    }
    public boolean resetModule(Descriptor descriptor){
        boolean[] suc ={false};
        DataStore dataStore = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.bucketId(),descriptor.typeId());
        if(!dataStore.load(lobbyTypeIdIndex)){
            if(descriptor.index()!=null){
                IndexSet indexSet = new IndexSet();
                indexSet.distributionKey(descriptor.index());
                indexSet.label(ExposedGameService.INDEX_LABEL);
                if(dataStore.load(indexSet)){
                    indexSet.keySet().forEach((k)->{
                        DeploymentDescriptor app = new DeploymentDescriptor();
                        app.distributionKey(k);
                        if(dataStore.load(app)){
                            app.codebase(descriptor.codebase());
                            app.moduleArtifact(descriptor.moduleArtifact());
                            app.moduleVersion(descriptor.moduleVersion());
                            dataStore.update(app);
                            suc[0]=true;
                        }
                    });
                }
            }
            return suc[0];
        }
        dataStore.list(new ApplicationQuery(lobbyTypeIdIndex.index()),(a)->{
            a.codebase(descriptor.codebase());
            a.moduleArtifact(descriptor.moduleArtifact());
            a.moduleVersion(descriptor.moduleVersion());
            dataStore.update(a);
            suc[0]=true;
            return true;
        });
        return suc[0];
    }
    @Override
    public void memberAdded(MembershipServiceEvent membershipServiceEvent) {
        Member lm = nodeEngine.getLocalMember();
        int sz = nodeEngine.getClusterService().getSize();
        int pt = 0;
        for(Member m : nodeEngine.getClusterService().getMembers()){
            if(lm.getUuid().equals(m.getUuid())){
                break;
            }
            pt++;
        }
        log.warn("bucket receiver updating on member added->["+pt+"/"+sz+"]"+lm.getUuid());
        for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
            this.tarantulaContext.integrationCluster().onPartition(i,i%sz==pt);
        }
    }

    @Override
    public void memberRemoved(MembershipServiceEvent membershipServiceEvent) {
        Member lm = nodeEngine.getLocalMember();
        int sz = nodeEngine.getClusterService().getSize();
        int pt = 0;
        for(Member m : nodeEngine.getClusterService().getMembers()){
            if(lm.getUuid().equals(m.getUuid())){
                break;
            }
            pt++;
        }
        log.warn("bucket receiver updating on member removed->["+pt+"/"+sz+"]"+lm.getUuid());
        for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
            this.tarantulaContext.integrationCluster().onPartition(i,i%sz==pt);
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeServiceEvent memberAttributeServiceEvent) {
    }

    public boolean onEnableGameCluster(String gameClusterId){
        return this.deploymentServiceProvider.distributionCallback().onGameClusterEnabled(gameClusterId);
    }
    public boolean onDisableGameCluster(String gameClusterId){
        return this.deploymentServiceProvider.distributionCallback().onGameClusterDisabled(gameClusterId);
    }


    public void onUpload(String fileName,byte[] content){
        this.tarantulaContext._writeContent(fileName,content);
    }
    public void startGameService(String gameClusterKey){
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterKey);
        this.deploymentServiceProvider.distributionCallback().addGameService(gameCluster);
    }
    public void onLaunchGameCluster(String gameClusterKey){
        this.deploymentServiceProvider.distributionCallback().onGameClusterLaunched(gameClusterKey);
    }
    public void onShutdownGameCluster(String gameClusterKey){
        this.deploymentServiceProvider.distributionCallback().onGameClusterShutdown(gameClusterKey);
    }
    public void onLaunchApplication(String typeId,String applicationId){
        this.deploymentServiceProvider.distributionCallback().onApplicationLaunched(typeId,applicationId);
    }
    public void onShutdownApplication(String typeId,String applicationId){
        this.deploymentServiceProvider.distributionCallback().onApplicationShutdown(typeId,applicationId);
    }
    public void launchModule(String typeId){
        this.deploymentServiceProvider.distributionCallback().addLobby(typeId);
    }
    public void shutdownModule(String typeId){
        this.deploymentServiceProvider.distributionCallback().removeLobby(typeId);
    }
    public void updateModule(Descriptor descriptor){
        this.deploymentServiceProvider.distributionCallback().updateModule(descriptor);
    }
    public void onUpdateResource(String contentUrl,String resourceName){
        this.deploymentServiceProvider.distributionCallback().onResourceUpdated(contentUrl,resourceName);
    }
    public void deployModule(String contentUrl,String resourceName){
        this.deploymentServiceProvider.distributionCallback().updateModule(contentUrl,resourceName);
    }

    public void onUpdateConfigurable(String key){
        this.deploymentServiceProvider.distributionCallback().onConfigurableUpdated(key);
    }

    public boolean onRegisterChannel(String typeId,Channel channel){
        return this.deploymentServiceProvider.distributionCallback().onChannelRegistered(typeId,channel);
    }
    public void onRegisterConnection(String typeId,Connection connection){
        this.deploymentServiceProvider.distributionCallback().onConnectionRegistered(typeId,connection);
    }
    public void onReleaseConnection(String typeId,Connection connection){
        this.deploymentServiceProvider.distributionCallback().onConnectionReleased(typeId,connection);
    }
    public void onVerifyConnection(String typeId,String serverId){
        this.deploymentServiceProvider.distributionCallback().onConnectionVerified(typeId,serverId);
    }

    public byte[] onClusterKey() {
        return this.tarantulaContext.tokenValidatorProvider().clusterKey(this.tarantulaContext.clusterNameSuffix());
    }

    public void onResetClusterKey() {
        this.tarantulaContext.tokenValidatorProvider().reset();
    }
    public void onEnablePresenceService(String root,String password,String clusterNameSuffix,String host) {
        this.tarantulaContext.tokenValidatorProvider().enablePresenceService(root,password,clusterNameSuffix,host);
    }
    public void onDisablePresenceService(String clusterNameSuffix) {
        this.tarantulaContext.tokenValidatorProvider().disablePresenceService(clusterNameSuffix);
    }
    @Override
    public void migrationStarted(MigrationEvent migrationEvent) {

    }

    @Override
    public void migrationCompleted(MigrationEvent migrationEvent) {
        this.tarantulaContext.integrationCluster().onReload(migrationEvent.getPartitionId(),migrationEvent.getNewOwner().localMember());
    }

    @Override
    public void migrationFailed(MigrationEvent migrationEvent) {

    }
}
