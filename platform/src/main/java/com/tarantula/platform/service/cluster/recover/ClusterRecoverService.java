package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.RecoverService;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.event.EventOnReplication;
import com.tarantula.platform.event.IntegrationReplicationEvent;
import com.tarantula.platform.event.KeyIndexEvent;
import com.tarantula.platform.event.OnReplicationEvent;
import com.tarantula.platform.service.cluster.accessindex.AccessIndexServiceBootstrap;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.service.persistence.ReplicationData;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.persistence.RevisionObject;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusterRecoverService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(ClusterRecoverService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    private int scope;
    private AtomicInteger _total = new AtomicInteger(0);
    private Thread replicationWriter;
    private ArrayList<OnReplication> pendingUpdates;
    private boolean running = true;
    private ArrayList<OnReplication> updates;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.scope = Integer.parseInt(properties.getProperty("tarantula-scope"));
        tarantulaContext = TarantulaContext.getInstance();
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
                            this.tarantulaContext.dataStore(r.source(),tarantulaContext.node().partitionNumber()).backup().set(r.key(),r.value());
                            KeyIndexEvent keyIndexEvent = new KeyIndexEvent(r.source(),new String(r.key()),r.nodeName(),this.tarantulaContext.node().nodeName());
                            this.tarantulaContext.keyIndexService.onReplicated(keyIndexEvent);
                        });
                        updates.clear();
                    }
                    Thread.sleep(10);
                }catch (Exception ex){
                    //ignore
                    ex.printStackTrace();
                }
            }
            log.warn("Stopping data replication thread");
        },"tarantula-data-replication-writer");
        replicationWriter.start();
        new ServiceBootstrap(TarantulaContext._integrationClusterStarted,TarantulaContext._recoverServiceStarted,new RecoverServiceBootstrap(this),"recover-service",true).start();
        log.warn("Cluster Recover Service Started on scope ["+scope+"]");
    }

    public void setup() throws Exception{
        this.tarantulaContext.clusterProvider().subscribe(tarantulaContext.node().nodeName()+"."+ RecoverService.NAME, event -> {
            if(event instanceof EventOnReplication){
                OnReplicationEvent dataReplicationEvent = (OnReplicationEvent)event;
                KeyIndexEvent keyIndexEvent = new KeyIndexEvent(dataReplicationEvent.source(),this.tarantulaContext.node().nodeName());
                String[] sources = new String[dataReplicationEvent.data().length];
                String[] keys = new String[dataReplicationEvent.data().length];
                for(int i=0;i<dataReplicationEvent.data().length;i++){
                    OnReplication r = dataReplicationEvent.data()[i];
                    DataStore dataStore = tarantulaContext.dataStore(r.source(),tarantulaContext.node().partitionNumber());
                    dataStore.backup().set(r.key(),r.value());
                    sources[i]=r.source();
                    keys[i]=new String(r.key());
                }
                keyIndexEvent.owners = sources;
                keyIndexEvent.keys = keys;
                tarantulaContext.keyIndexService().onReplicated(keyIndexEvent);
            }
            return false;
        });
        TarantulaContext._cluster_service_ready.countDown();
    }
    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("Cluster is shutting down");
        running = false;
    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new RecoverServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public void delete(String  source,byte[] key){
        this.tarantulaContext.dataStore(source,tarantulaContext.node().partitionNumber()).backup().unset(key);
    }
    public byte[] load(String source,byte[] key){
        return this.tarantulaContext.dataStore(source,tarantulaContext.node().partitionNumber()).backup().get(key);
    }
    public void replicate(OnReplication[] onReplications){
        synchronized (pendingUpdates){
            for(OnReplication onReplication : onReplications){
                pendingUpdates.add(onReplication);
            }
        }
    }
    public void replicate(String nodeName,String source,byte[] key,byte[] value){
        synchronized (pendingUpdates){
            pendingUpdates.add(new ReplicationData(nodeName,source,key,value));
        }
    }
    public int syncStart(String memberId,String source,String syncKey){
        RecoverService recoverService = tarantulaContext.integrationCluster().recoverService();
        new Thread(()->{
            int[] total={0};
            long st = System.currentTimeMillis();
            if(!memberId.equals(nodeEngine.getLocalMember().getUuid())){
                int[] batch={0};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                this.tarantulaContext.dataStore(source,this.tarantulaContext.node().partitionNumber()).backup().list((k,v)->{
                    if(batch[0] == tarantulaContext.recoverBatchSize){
                        recoverService.onSync(batch[0],keys,values,memberId,source);
                        batch[0] = 0;
                    }
                    keys[batch[0]]=k;
                    values[batch[0]]=v;
                    batch[0]++;
                    total[0]++;
                    return true;
                });
                //last batch
                recoverService.onSync(batch[0],keys,values,memberId,source);
            }
            recoverService.onEndSync(memberId,syncKey);
            log.warn("Total records ["+total[0]+"] from ["+source+"] synced to ["+memberId+"] timed (seconds) ["+((System.currentTimeMillis()-st)/1000)+"]");
        }).start();
        return this.tarantulaContext.node().partitionNumber();
    }
    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            replicate(d.nodeName(),d.source(),d.key(),d.value());
        }
        _total.addAndGet(batch.length);
    }
    public void syncEnd(String syncKey){
        int tc = _total.getAndSet(0);
        tarantulaContext._syncLatch.get(syncKey).countDown();
        log.warn("Total records received ["+tc+"] from master node"+">>"+syncKey);
    }


    public String[] onListModules(){
        return this.tarantulaContext._listModuleContent();
    }
    public byte[] onLoadModuleJarFile(String fileName){
        return this.tarantulaContext._readContent(fileName);
    }


}
