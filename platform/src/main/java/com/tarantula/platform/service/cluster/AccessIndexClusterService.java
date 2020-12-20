package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.*;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ReplicationData;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;

import java.util.Properties;

/**
 * Updated by yinghu lu on 6/18/2020
 */
public class AccessIndexClusterService implements ManagedService,RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;

    private DataStoreOnPartition[] dataStoreOnPartitions;
    private DataStore masterStore;
    private PartitionIndex localKey;
    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.tarantulaContext = TarantulaContext.getInstance();
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


    public AccessIndex set(String accessKey){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        int pid = localKey.count(1);
        localKey.update();//TO DO ID SEGMENT TO REDUCE DISK WRITES
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,dso.partitionIndex.bucket(),dso.partitionIndex.label(),pid);
        return dso.dataStore.createIfAbsent(accessIndex,false)?accessIndex:null;
    }
    public AccessIndex get(String accessKey){
        AccessIndex suc = new AccessIndexTrack(accessKey);
        DataStore dataStore = this.onPartition(accessKey).dataStore;
        return dataStore.load(suc)?suc:null;
    }
    public void enable(){
        this.deploymentServiceProvider.distributionCallback().startAccessIndex();
        //for(DataStoreOnPartition ds : dataStoreOnPartitions){
            //ds.dataStore.backup().list((k,v)-> {
                //log.warn(new String(k));
                //return true;
            //});
        //}
    }
    public void disable(){
        this.deploymentServiceProvider.distributionCallback().stopAccessIndex();
    }
    public int sync(String memberId){
        new Thread(()->{
            int[] batch = {0};
            byte[][] keys = new byte[10][];
            byte[][] values = new byte[10][];
            for(DataStoreOnPartition ds : dataStoreOnPartitions){
                ds.dataStore.backup().list((k,v)-> {
                    log.warn("key=>"+new String(k)+"<><><>"+ds.partition);
                    if(batch[0] == 10){
                        log.warn("Batch->"+batch[0]);
                        this.tarantulaContext.accessIndexService().sync(10,keys,values,memberId);
                        batch[0] = 0;
                    }
                    keys[batch[0]]=k;
                    values[batch[0]]=v;
                    batch[0]++;
                    return true;
                });
            }
            log.warn("Last batch->"+batch[0]);
            //last batch
            this.tarantulaContext.accessIndexService().sync(batch[0],keys,values,memberId);
            this.tarantulaContext.accessIndexService().syncEnd(memberId);
        }).start();
        return nodeEngine.getPartitionService().getPartitionCount();
    }
    public void setup() {
        this.deploymentServiceProvider = this.tarantulaContext.deploymentServiceProvider();
        masterStore = this.tarantulaContext.masterDataStore();
        localKey = new PartitionIndex(masterStore.bucket(),masterStore.node()+"-partition-id","",1000);
        localKey.dataStore(masterStore);
        masterStore.createIfAbsent(localKey,true);
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStore(dso.name);
            dso.partitionIndex = new PartitionIndex(this.masterStore.bucket(),this.masterStore.node(),dso.name,1000);
        }
        log.warn("Access index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]");
    }
    private DataStoreOnPartition onPartition(String accessKey){
        int partition = this.nodeEngine.getPartitionService().getPartitionId(accessKey);
        return this.dataStoreOnPartitions[partition];
    }
    public int getPartitionId(byte[] key){
        return this.nodeEngine.getPartitionService().getPartitionId(key);
    }
    public void replicate(int partition,byte[] key,byte[] value){
        log.warn("Replicating ["+new String(key)+"]->"+partition+"<><><>"+new String(value));
        //this.dataStoreOnPartitions[partition].dataStore.backup().set(key,value);
    }
    public void syncEnd(){
        TarantulaContext._syc_finished.countDown();
    }
    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            replicate(d.partition,d.key,d.value);
        }
    }
    public byte[] recover(int partition,byte[] key){
        //log.warn("Recovering from ["+partition+"]->"+new String(key));
        return this.dataStoreOnPartitions[partition].dataStore.backup().get(key);
    }
}
