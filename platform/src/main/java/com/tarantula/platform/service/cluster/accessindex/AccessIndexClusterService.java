package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;
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
import com.tarantula.platform.util.SystemUtil;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class AccessIndexClusterService implements ManagedService,RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;

    private DataStoreOnPartition[] dataStoreOnPartitions;

    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private AtomicInteger total = new AtomicInteger(0);
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
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex _try = new AccessIndexTrack(accessKey);
        if(dso.dataStore.load(_try)){
            return null;
        }
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket, SystemUtil.oid(),referenceId);
        return dso.dataStore.createIfAbsent(accessIndex,false)?accessIndex:null;
    }
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex _try = new AccessIndexTrack(accessKey);
        if(dso.dataStore.load(_try)){
            return _try;
        }
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket,SystemUtil.oid(),referenceId);
        dso.dataStore.createIfAbsent(accessIndex,true);
        return accessIndex;
    }
    public AccessIndex get(String accessKey){
        AccessIndex suc = new AccessIndexTrack(accessKey);
        DataStore dataStore = this.onPartition(accessKey).dataStore;
        return dataStore.load(suc)?suc:null;
    }
    public void enable(){
        this.deploymentServiceProvider.distributionCallback().startAccessIndex();
    }
    public void disable(){
        this.deploymentServiceProvider.distributionCallback().stopAccessIndex();
    }
    public int sync(String memberId){
        new Thread(()->{
            int[] total={0};
            long st = System.currentTimeMillis();
            if(!memberId.equals(nodeEngine.getLocalMember().getUuid())){
                int[] batch = {0};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                for(DataStoreOnPartition ds : dataStoreOnPartitions){
                    ds.dataStore.backup().list((k,v)-> {
                        if(batch[0] == tarantulaContext.recoverBatchSize){
                            this.tarantulaContext.accessIndexService().sync(tarantulaContext.recoverBatchSize,keys,values,memberId);
                            batch[0] = 0;
                        }
                        keys[batch[0]]=k;
                        values[batch[0]]=v;
                        batch[0]++;
                        total[0]++;
                        return true;
                    });
                }
                //last batch
                this.tarantulaContext.accessIndexService().sync(batch[0],keys,values,memberId);
            }
            this.tarantulaContext.accessIndexService().syncEnd(memberId);
            log.warn("Total access index records ["+total[0]+"] synced to ["+memberId+"] timed (seconds) ["+((System.currentTimeMillis()-st)/1000)+"]");
        }).start();
        return nodeEngine.getPartitionService().getPartitionCount();
    }

    public void setup() {
        this.deploymentServiceProvider = this.tarantulaContext.deploymentServiceProvider();
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStore(dso.name);
        }
        log.warn("Access index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]["+bucket+"]");
    }

    private DataStoreOnPartition onPartition(String accessKey){
        int partition = this.nodeEngine.getPartitionService().getPartitionId(accessKey);
        return this.dataStoreOnPartitions[partition];
    }

    private int getPartitionId(byte[] key){
        return this.nodeEngine.getPartitionService().getPartitionId(key);
    }

    public void replicate(int partition,byte[] key,byte[] value){
        this.dataStoreOnPartitions[partition].dataStore.backup().set(key,value);
    }

    public void syncEnd(){
        TarantulaContext._syc_finished.countDown();
        log.warn("Total access index records received ["+total.get()+"] from master node");
    }

    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            d.partition = getPartitionId(d.key);
            replicate(d.partition,d.key,d.value);
        }
        total.addAndGet(batch.length);
    }

    public byte[] recover(int partition,byte[] key){
        return this.dataStoreOnPartitions[partition].dataStore.backup().get(key);
    }
}
