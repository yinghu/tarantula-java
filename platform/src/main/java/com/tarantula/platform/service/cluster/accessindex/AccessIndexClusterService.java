package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;

import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.*;

import com.icodesoftware.lmdb.LocalMetadata;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;

import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;

import com.tarantula.platform.event.TransactionReplicationEvent;


import java.util.Properties;


public class AccessIndexClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;


    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private String bucket;


    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.tarantulaContext = TarantulaContext.getInstance();
        this.bucket = this.tarantulaContext.dataBucketGroup;
        this.nodeEngine = nodeEngine;
        new ServiceBootstrap(TarantulaContext._integrationClusterStarted,TarantulaContext._accessIndexServiceStarted,new AccessIndexServiceBootstrap(this),"access-index-service",true).start();
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.info("Shutting down access index cluster service");
    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new AccessIndexServiceProxy(objectName,this.nodeEngine,this);
    }


    @Override
    public void destroyDistributedObject(String objectName) {
        log.warn(objectName+" destroyed");//call from proxy
    }


    public AccessIndex set(String accessKey,int referenceId){
        DataStore dataStore = this.dataStore();
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,referenceId,tarantulaContext.distributionId());
        if(!dataStore.createIfAbsent(accessIndex,false)) return null;
        return accessIndex;
    }
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        DataStore dataStore = this.dataStore();
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,referenceId,tarantulaContext.distributionId());
        dataStore.createIfAbsent(accessIndex,true);
        return accessIndex;
    }
    public AccessIndex get(String accessKey){
        DataStore dataStore = this.dataStore();
        AccessIndex accessIndex = new AccessIndexTrack(accessKey);
        if(!dataStore.load(accessIndex)) return null;
        return accessIndex;
    }
    public boolean delete(String accessKey){
        DataStore dataStore = this.dataStore();
        AccessIndex accessIndex = new AccessIndexTrack(accessKey);
        if(!dataStore.load(accessIndex)) return false;

        return dataStore.delete(accessIndex);
    }
    public void enable(){
        if(deploymentServiceProvider==null) return;
        this.deploymentServiceProvider.distributionCallback().onAccessIndexEnabled();
    }
    public void disable(){
        if(deploymentServiceProvider==null) return;
        this.deploymentServiceProvider.distributionCallback().onAccessIndexDisabled();
    }

    public void setup() throws Exception{
        TarantulaContext._integrationClusterStarted.await();
        this.deploymentServiceProvider = this.tarantulaContext.deploymentServiceProvider();
        this.tarantulaContext.clusterProvider().subscribe(MapStoreListener.INTEGRATION_MAP_STORE_NAME, event -> {
            if(event.source().equals(tarantulaContext.node().nodeName())) return false;
            if(event instanceof TransactionReplicationEvent){
                tarantulaContext.onTransactionEvent(Distributable.INTEGRATION_SCOPE,(TransactionReplicationEvent)event);
            }
            return false;
        });
        TarantulaContext._cluster_service_ready.countDown();
        log.info("Access index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]["+bucket+"]");
    }

    public byte[] recover(byte[] key){
        TransactionLogManager transactionLogManager = this.tarantulaContext.transactionLogManager(Distributable.INTEGRATION_SCOPE);
        Metadata metadata = new LocalMetadata(Distributable.INTEGRATION_SCOPE,AccessIndexService.STORE_NAME);
        return transactionLogManager.loadFromCommitted(metadata,key);
    }

    public void replicate(TransactionReplicationEvent transactionReplicationEvent){
        tarantulaContext.onTransactionEvent(Distributable.INTEGRATION_SCOPE,transactionReplicationEvent);
    }

    private DataStore dataStore(){
        return this.tarantulaContext.deploymentDataStoreProvider.createAccessIndexDataStore(AccessIndexService.STORE_NAME);
    }
}
