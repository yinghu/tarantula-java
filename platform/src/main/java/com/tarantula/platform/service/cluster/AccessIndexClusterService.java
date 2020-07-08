package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;

import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.spi.*;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;

import java.util.Properties;

/**
 * Updated by yinghu lu on 6/18/2020
 */
public class AccessIndexClusterService implements ManagedService,RemoteService, MigrationListener {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;

    private DataStoreOnPartition[] dataStoreOnPartitions;
    private DataStore masterStore;
    private PartitionIndex localKey;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.tarantulaContext = TarantulaContext.getInstance();
        this.nodeEngine = nodeEngine;
        this.nodeEngine.getPartitionService().addMigrationListener(this);
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


    public AccessIndex set(String accessKey){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        if(dso.enabled.get()){
            int pid = localKey.count(1);
            localKey.update();
            AccessIndex accessIndex = new AccessIndexTrack(accessKey,dso.partitionIndex.bucket(),dso.partitionIndex.label(),pid);
            log.warn("KEY->"+accessIndex.distributionKey());
            if(dso.dataStore.createIfAbsent(accessIndex,false)){
                return accessIndex;
            }
            else{
                return null;
            }
        }
        else{
            log.warn("Partition ["+dso.partition+"] not available");
            return null;
        }
    }
    public AccessIndex get(String accessKey){
        AccessIndex suc = new AccessIndexTrack(accessKey);
        DataStore dataStore = this.onPartition(accessKey).dataStore;
        if(dataStore.load(suc)){
            return suc;
        }else{
            return null;
        }
    }
    public void setup() {
        masterStore = this.tarantulaContext.masterDataStore();
        localKey = new PartitionIndex(masterStore.bucket(),masterStore.node()+"-nodeId","",1000);
        localKey.dataStore(masterStore);
        masterStore.createIfAbsent(localKey,true);
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStore(dso.name);
            dso.partitionIndex = new PartitionIndex(this.masterStore.bucket(),this.masterStore.node(),dso.name,1000);
            boolean loc = this.nodeEngine.getPartitionService().getPartition(dso.partition).isLocal();
            dso.local.set(loc);
            dso.enabled.set(loc);
        }
        log.warn("Access index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]");
    }
    private DataStoreOnPartition onPartition(String accessKey){
        int partition = this.nodeEngine.getPartitionService().getPartitionId(accessKey);
        return this.dataStoreOnPartitions[partition];
    }

    private void onPartition(String memberId,int partition){
        DataStoreOnPartition dso = this.dataStoreOnPartitions[partition];
        if(memberId.equals(this.tarantulaContext.integrationCluster().subscription())){
            dso.local.set(true);
            dso.enabled.set(true);
        }
        else{
            dso.reset();
        }
    }

    @Override
    public void migrationStarted(MigrationEvent migrationEvent) {

    }

    @Override
    public void migrationCompleted(MigrationEvent migrationEvent) {
        this.onPartition(migrationEvent.getNewOwner().getUuid(),migrationEvent.getPartitionId());
    }

    @Override
    public void migrationFailed(MigrationEvent migrationEvent) {

    }


}
