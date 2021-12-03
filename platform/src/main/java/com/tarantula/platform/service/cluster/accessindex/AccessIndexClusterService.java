package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.*;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.util.SystemUtil;

import java.util.Properties;

public class AccessIndexClusterService implements ManagedService,RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;

    private DataStoreOnPartition[] dataStoreOnPartitions;

    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private AccessIndexService.AccessIndexStore accessIndexStore;
    private String bucket;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.tarantulaContext = TarantulaContext.getInstance();
        this.bucket = this.tarantulaContext.bucket();
        this.nodeEngine = nodeEngine;
        this.dataStoreOnPartitions = new DataStoreOnPartition[this.nodeEngine.getPartitionService().getPartitionCount()];
        for(int i=0;i<this.dataStoreOnPartitions.length;i++){
            this.dataStoreOnPartitions[i]=new DataStoreOnPartition(i,"p_"+i);
        }
        new ServiceBootstrap(tarantulaContext._storageStarted,tarantulaContext._accessIndexServiceStarted,new AccessIndexServiceBootstrap(this),"access-index-service",true).start();
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("Shutting down access index cluster service");
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
        byte[] key = accessKey.getBytes();
        if(this.accessIndexStore.available(key)) return null;
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket, SystemUtil.oid(),referenceId);
        if(!dso.dataStore.createIfAbsent(accessIndex,false)) return null;
        this.accessIndexStore.setAccessIndex(key,accessIndex);
        return accessIndex;
    }
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket,SystemUtil.oid(),referenceId);
        dso.dataStore.createIfAbsent(accessIndex,true);
        return accessIndex;
    }
    public AccessIndex get(String accessKey){
        return accessIndexStore.getAccessIndex(accessKey.getBytes());//new AccessIndexTrack(accessKey);
        //DataStore dataStore = this.onPartition(accessKey).dataStore;
        //return dataStore.load(suc)?suc:null;
    }
    public void enable(){
        this.deploymentServiceProvider.distributionCallback().startAccessIndex();
    }
    public void disable(){
        this.deploymentServiceProvider.distributionCallback().stopAccessIndex();
    }


    public void setup() throws Exception{
        TarantulaContext._integrationClusterStarted.await();
        this.deploymentServiceProvider = this.tarantulaContext.deploymentServiceProvider();
        this.accessIndexStore = this.tarantulaContext.integrationCluster().accessIndexStore();
        int[] totalLoaded = {0};
        log.warn("Loading access index from local storage. It will take some time due to data size.");
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStore(dso.name);
            dso.dataStore.backup().list((k,v)->{
                totalLoaded[0]++;
                AccessIndex accessIndex = new AccessIndexTrack();
                accessIndex.fromBinary(v);
                this.accessIndexStore.setAccessIndex(k,accessIndex);
                return true;
            });
        }
        TarantulaContext._syc_finished.countDown();
        log.warn("Access index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]["+bucket+"] with total access index loaded ["+totalLoaded[0]+"]");
    }

    private DataStoreOnPartition onPartition(String accessKey){
        int partition = this.nodeEngine.getPartitionService().getPartitionId(accessKey);
        return this.dataStoreOnPartitions[partition];
    }

    public void replicate(int partition,byte[] key,byte[] value){
        this.dataStoreOnPartitions[partition].dataStore.backup().set(key,value);
    }

    public byte[] recover(int partition,byte[] key){
        return this.dataStoreOnPartitions[partition].dataStore.backup().get(key);
    }
}
