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
        new ServiceBootstrap(TarantulaContext._integrationClusterStarted,TarantulaContext._deployServiceStarted,new DeployServiceBootstrap(this),"deploy-service",true).start();
    }
    public void setup(){
        TarantulaContext._cluster_service_ready.countDown();
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

    public void onUpdateView(OnView onView){
        this.deploymentServiceProvider.distributionCallback().onViewUpdated(onView);
    }

    @Override
    public void memberAdded(MembershipServiceEvent membershipServiceEvent) {
        Member lm = nodeEngine.getLocalMember();
        //log.warn(">>>>>>>>>>>>>>>>>>>>>"+nodeEngine.getClusterService().getMember(lm.getUuid()).getUuid());
        //if(!lm.getUuid().equals(membershipServiceEvent.getMember().getUuid())){
            //this.tarantulaContext.integrationCluster().onNodeAdded(membershipServiceEvent.getMember().getUuid());
        //}
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
        this.tarantulaContext.integrationCluster().onNodeRemoved(membershipServiceEvent);
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
        this.tarantulaContext.integrationCluster().onNodeRegistered(memberAttributeServiceEvent);
    }

    public void onUpload(String fileName,byte[] content){
        this.tarantulaContext._writeContent(fileName,content);
    }
    public void onStartGameService(String gameClusterKey){
        this.deploymentServiceProvider.distributionCallback().onGameServiceStarted(gameClusterKey);
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
    public void onLaunchModule(String typeId){
        this.deploymentServiceProvider.distributionCallback().onModuleLaunched(typeId);
    }
    public void onShutdownModule(String typeId){
        this.deploymentServiceProvider.distributionCallback().onModuleShutdown(typeId);
    }
    public void onUpdateModule(Descriptor descriptor){
        this.deploymentServiceProvider.distributionCallback().onModuleUpdated(descriptor);
    }
    public void onUpdateResource(String contentUrl,String resourceName){
        this.deploymentServiceProvider.distributionCallback().onResourceUpdated(contentUrl,resourceName);
    }
    public void onDeployModule(String contentUrl,String resourceName){
        this.deploymentServiceProvider.distributionCallback().onModuleDeployed(contentUrl,resourceName);
    }

    public void onUpdateConfigurable(String key){
        this.deploymentServiceProvider.distributionCallback().onConfigurableUpdated(key);
    }

    public void onStartConnection(String typeId,Connection connection){
        this.deploymentServiceProvider.distributionCallback().onConnectionStarted(typeId,connection);
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
        return this.tarantulaContext.tokenValidatorProvider().clusterKey(this.tarantulaContext.node().clusterNameSuffix());
    }
    public byte[] onTokenKey() {
        return this.tarantulaContext.tokenValidatorProvider().tokenKey(this.tarantulaContext.node().clusterNameSuffix());
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
