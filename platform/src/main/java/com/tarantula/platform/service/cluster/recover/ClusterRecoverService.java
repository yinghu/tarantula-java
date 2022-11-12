package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.RecoverService;
import com.tarantula.platform.service.ReplicationData;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;

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
    private ArrayList<ReplicationData> updates;
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
                        pendingUpdates.removeAll(updates);
                    }
                    if(updates.size()>0){
                        log.warn("Total data pending size->"+updates.size());
                        updates.forEach(r->{
                            this.tarantulaContext.dataStore(r.source,tarantulaContext.node().partitionNumber()).backup().set(r.key,r.value);
                        });
                        updates.clear();
                    }
                    Thread.sleep(10);
                }catch (Exception ex){
                    //ignore
                }
            }
            log.warn("Stopping data replication thread");
        },"tarantula-data-replication-writer");
        replicationWriter.start();
        log.warn("Cluster Recover Service Started on scope ["+scope+"]");
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
    public void replicate(String source,byte[] key,byte[] value){
        synchronized (pendingUpdates){
            pendingUpdates.add(new ReplicationData(source,key,value));
        }
        //this.tarantulaContext.dataStore(source,tarantulaContext.node().partitionNumber()).backup().set(key,value);
    }
    public int syncStart(String memberId,String source,String syncKey){
        RecoverService recoverService = tarantulaContext.integrationCluster().recoverService();
        new Thread(()->{
            int[] total={0};
            //long st = System.currentTimeMillis();
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
            //log.warn("Total records ["+total[0]+"] from ["+source+"] synced to ["+memberId+"] timed (seconds) ["+((System.currentTimeMillis()-st)/1000)+"]");
        }).start();
        return this.tarantulaContext.node().partitionNumber();
    }
    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            replicate(d.source,d.key,d.value);
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
