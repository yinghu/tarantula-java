package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;

import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.EventService;
import com.icodesoftware.service.RecoverService;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.event.AccessIndexSyncEvent;
import com.tarantula.platform.service.ReplicationData;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.util.SystemUtil;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AccessIndexClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;

    private DataStoreOnPartition[] dataStoreOnPartitions;

    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private ConcurrentHashMap<String,AccessIndex> accessCache;
    private String bucket;
    private EventService publisher;

    private AtomicInteger _total = new AtomicInteger(0);

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.tarantulaContext = TarantulaContext.getInstance();
        accessCache = new ConcurrentHashMap<>();
        this.bucket = this.tarantulaContext.bucket();
        this.nodeEngine = nodeEngine;
        this.dataStoreOnPartitions = new DataStoreOnPartition[this.nodeEngine.getPartitionService().getPartitionCount()];
        for(int i=0;i<this.dataStoreOnPartitions.length;i++){
            this.dataStoreOnPartitions[i]=new DataStoreOnPartition(i, AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX +i);
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
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket, SystemUtil.oid(),referenceId);
        if(this.accessCache.putIfAbsent(accessKey,accessIndex)!=null) return null;//block duplicate access key
        if(!dso.dataStore.createIfAbsent(accessIndex,false)) return null;
        publisher.publish(new AccessIndexSyncEvent(AccessIndexService.NAME,accessKey,accessIndex.toBinary()));
        return accessIndex;
    }
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket,SystemUtil.oid(),referenceId);
        dso.dataStore.createIfAbsent(accessIndex,true);
        return accessIndex;
    }
    public AccessIndex get(String accessKey){
        AccessIndex accessIndex = accessCache.get(accessKey);
        if(accessIndex!=null) return accessIndex;
        DataStoreOnPartition dso = this.onPartition(accessKey);
        accessIndex = new AccessIndexTrack(accessKey);
        if(!dso.dataStore.load(accessIndex)) return null;
        accessCache.put(accessKey,accessIndex);
        return accessIndex;
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
        this.tarantulaContext.integrationCluster().registerEventListener(AccessIndexService.NAME,e->{
            byte[] key = e.trackId().getBytes();
            DataStoreOnPartition dso = this.onPartition(e.trackId());
            if(dso.dataStore.backup().get(key)==null) dso.dataStore.backup().set(key,e.payload());
            return true;
        });
        this.publisher = this.tarantulaContext.integrationCluster().publisher();
        int[] totalLoaded = {0};
        log.warn("Loading access index from local storage. It will take some time due to data size.");
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStore(dso.name);
            dso.dataStore.backup().list((k,v)->{
                totalLoaded[0]++;
                AccessIndex accessIndex = new AccessIndexTrack();
                accessIndex.fromBinary(v);
                String aKey = new String(k);
                this.accessCache.put(aKey,accessIndex);
                publisher.publish(new AccessIndexSyncEvent(AccessIndexService.NAME,aKey,v));//redundancy sync
                return true;
            });
        }
        TarantulaContext._access_index_syc_finished.countDown();
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
    public int syncStart(String memberId,int partition,String syncKey){
        AccessIndexService recoverService = tarantulaContext.integrationCluster().accessIndexService();
        new Thread(()->{
            int[] total={0};
            long st = System.currentTimeMillis();
            if(!memberId.equals(nodeEngine.getLocalMember().getUuid())){
                int[] batch={0};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                this.dataStoreOnPartitions[partition].dataStore.backup().list((k,v)->{
                    if(batch[0] == tarantulaContext.recoverBatchSize){
                        recoverService.sync(batch[0],keys,values,memberId,partition);
                        batch[0] = 0;
                    }
                    keys[batch[0]]=k;
                    values[batch[0]]=v;
                    batch[0]++;
                    total[0]++;
                    return true;
                });
                //last batch
                recoverService.sync(batch[0],keys,values,memberId,partition);
            }
            recoverService.syncEnd(memberId,syncKey);
            log.warn("Total records ["+total[0]+"] from ["+partition+"] synced to ["+memberId+"] timed (seconds) ["+((System.currentTimeMillis()-st)/1000)+"]");
        }).start();
        return this.tarantulaContext.partitionNumber();
    }
    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            replicate(d.partition,d.key,d.value);
        }
        _total.addAndGet(batch.length);
    }
    public void syncEnd(String syncKey){
        tarantulaContext._syncLatch.get(syncKey).countDown();
        log.warn("Total records received ["+_total.get()+"] from master node"+">>"+syncKey);
        _total.set(0);
    }
}
