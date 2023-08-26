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
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;

import com.tarantula.platform.event.EventOnReplication;
import com.tarantula.platform.event.IntegrationReplicationEvent;
import com.tarantula.platform.event.KeyIndexEvent;
import com.tarantula.platform.event.OnReplicationEvent;

import com.tarantula.platform.service.persistence.ReplicationData;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;

import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.Properties;


public class AccessIndexClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexClusterService.class);

    private NodeEngine nodeEngine;

    private DataStoreOnPartition[] dataStoreOnPartitions;

    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private String bucket;

    private Thread replicationWriter;
    private ArrayList<OnReplication> pendingUpdates;
    private boolean running = true;
    private ArrayList<OnReplication> updates;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.tarantulaContext = TarantulaContext.getInstance();
        this.bucket = this.tarantulaContext.dataBucketGroup;
        this.nodeEngine = nodeEngine;
        this.dataStoreOnPartitions = new DataStoreOnPartition[this.nodeEngine.getPartitionService().getPartitionCount()];
        for(int i=0;i<this.dataStoreOnPartitions.length;i++){
            this.dataStoreOnPartitions[i]=new DataStoreOnPartition(i, AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX +"x");
        }
        pendingUpdates = new ArrayList<>();
        updates = new ArrayList<>();
        replicationWriter = new Thread(()->{
            while (running){
                try{
                    synchronized (pendingUpdates){
                        pendingUpdates.forEach(c->updates.add(c));
                        pendingUpdates.clear();
                    }
                    if(updates.size()>0){
                        updates.forEach(r->{
                            DataStoreOnPartition dso = this.onPartition(r.partition());
                            dso.lock(r.key(),()-> dso.dataStore.backup().set(r.key(),r.value()));
                            KeyIndexEvent keyIndexEvent = new KeyIndexEvent(dso.name,new String(r.key()),r.nodeName(),this.tarantulaContext.node().nodeName());
                            tarantulaContext.keyIndexService().onReplicated(keyIndexEvent);
                        });
                        updates.clear();
                    }
                    Thread.sleep(10);
                }catch (Exception ex){
                    //ignore
                }
            }
            log.warn("Stopping access index replication thread");
        },"tarantula-access-index-replication-writer");
        replicationWriter.start();
        new ServiceBootstrap(TarantulaContext._integrationClusterStarted,TarantulaContext._accessIndexServiceStarted,new AccessIndexServiceBootstrap(this),"access-index-service",true).start();
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        this.running = false;
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
        byte[] key = accessKey.getBytes();
        boolean suc = dso.lock(key,()->
            dso.dataStore.createIfAbsent(accessIndex,false)
        );
        return suc?accessIndex:null;
    }
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex accessIndex = new AccessIndexTrack(accessKey,bucket,SystemUtil.oid(),referenceId);
        accessIndex.id(tarantulaContext.deploymentDataStoreProvider.nextId(dso.name));
        byte[] key = accessKey.getBytes();
        dso.lock(key,()-> dso.dataStore.createIfAbsent(accessIndex,true));
        return accessIndex;
    }
    public AccessIndex get(String accessKey){
        DataStoreOnPartition dso = this.onPartition(accessKey);
        AccessIndex accessIndex = new AccessIndexTrack(accessKey);
        byte[] key = accessKey.getBytes();
        boolean loaded = dso.lock(key,()-> dso.dataStore.load(accessIndex));
        if(!loaded) return null;
        return accessIndex;
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
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStoreProvider().createAccessIndexDataStore(dso.name);
        }

        this.tarantulaContext.clusterProvider().subscribe(tarantulaContext.node().nodeName()+"."+AccessIndexService.NAME,event -> {
            if(event instanceof EventOnReplication){
                OnReplicationEvent integrationReplicationEvent = (OnReplicationEvent)event;
                String[] sources = new String[integrationReplicationEvent.data().length];
                String[] keys = new String[integrationReplicationEvent.data().length];
                KeyIndexEvent keyIndexEvent = new KeyIndexEvent(integrationReplicationEvent.source(),this.tarantulaContext.node().nodeName());
                for(int i=0;i<integrationReplicationEvent.data().length;i++){
                    OnReplication r = integrationReplicationEvent.data()[i];
                    DataStoreOnPartition dso = this.onPartition(r.partition());
                    dso.lock(r.key(),()-> dso.dataStore.backup().set(r.key(),r.value()));
                    sources[i]=dso.name;
                    keys[i]=new String(r.key());
                }
                keyIndexEvent.owners = sources;
                keyIndexEvent.keys = keys;
                tarantulaContext.keyIndexService().onReplicated(keyIndexEvent);
            }
            return false;
        });
        TarantulaContext._cluster_service_ready.countDown();
        log.warn("Access index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]["+bucket+"]");
    }

    private DataStoreOnPartition onPartition(String accessKey){
        int partition = this.nodeEngine.getPartitionService().getPartitionId(accessKey);
        DataStoreOnPartition dso = this.dataStoreOnPartitions[partition];
        if(dso.dataStore==null) {
            dso.dataStore = this.tarantulaContext.dataStoreProvider().createAccessIndexDataStore(dso.name);
        }
        return dso;
    }
    private DataStoreOnPartition onPartition(int partition){
        DataStoreOnPartition dso = this.dataStoreOnPartitions[partition];
        if(dso.dataStore==null) {
            dso.dataStore = this.tarantulaContext.dataStoreProvider().createAccessIndexDataStore(dso.name);
        }
        return dso;
    }

    public void replicate(String nodeName,int partition,byte[] key,byte[] value){
        synchronized (pendingUpdates){
            pendingUpdates.add(new ReplicationData(nodeName,partition,key,value));
        }
    }
    public void replicate(OnReplication[] onReplications){
        synchronized (pendingUpdates){
            for(OnReplication onReplication : onReplications){
                pendingUpdates.add(onReplication);
            }
        }
    }

    public byte[] recover(int partition,byte[] key){
        return this.onPartition(partition).dataStore.backup().get(key);
    }
    public int syncStart(String memberId,int partition,String syncKey){
        AccessIndexService recoverService = tarantulaContext.integrationCluster().accessIndexService();
        new Thread(()->{
            if(!memberId.equals(nodeEngine.getLocalMember().getUuid())){
                int[] batch={0};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                this.onPartition(partition).dataStore.backup().list((k,v)->{
                    if(batch[0] == tarantulaContext.recoverBatchSize){
                        recoverService.onSync(batch[0],keys,values,memberId,partition);
                        batch[0] = 0;
                    }
                    keys[batch[0]]=k;
                    values[batch[0]]=v;
                    batch[0]++;
                    //total[0]++;
                    return true;
                });
                //last batch
                if(batch[0]>0) recoverService.onSync(batch[0],keys,values,memberId,partition);
            }
            recoverService.onEndSync(memberId,syncKey);
        }).start();
        return this.tarantulaContext.node().partitionNumber();
    }
    public void replicateAsBatch(String nodeName,OnReplication[] batch){
        for(OnReplication d : batch) {
            replicate(nodeName,d.partition(), d.key(), d.value());
        }
    }
    public void syncEnd(String syncKey){
        tarantulaContext._syncLatch.get(syncKey).countDown();
    }
}
