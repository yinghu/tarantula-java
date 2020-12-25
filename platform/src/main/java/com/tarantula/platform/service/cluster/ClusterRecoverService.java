package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.RecoverService;
import com.tarantula.platform.LobbyTypeIdIndex;
import com.tarantula.platform.service.ReplicationData;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusterRecoverService implements ManagedService, RemoteService {
    private static TarantulaLogger log = JDKLogger.getLogger(ClusterRecoverService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int scope;
    private AtomicInteger total = new AtomicInteger(0);
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.scope = Integer.parseInt(properties.getProperty("tarantula-scope"));
        tarantulaContext = TarantulaContext.getInstance();
        deploymentServiceProvider = tarantulaContext.deploymentService();
        log.warn("Cluster Recover Service Started on scope ["+scope+"]");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new RecoverServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }
    public String onDataNode(String source,byte[] key){
        if(load(source,key)!=null){
            return nodeEngine.getLocalMember().getUuid();
        }
        return null;
    }
    public String onModuleDataNode(String source,String typeId){
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.bucketId(),typeId);
        if(load(source,lobbyTypeIdIndex.key().asString().getBytes())!=null){
            return nodeEngine.getLocalMember().getUuid();
        }
        return null;
    }
    public byte[] load(String source,byte[] key){
        return this.tarantulaContext.dataStore(source,tarantulaContext.partitionNumber()).backup().get(key);
    }
    public void replicate(String source,byte[] key,byte[] value){
        this.tarantulaContext.dataStore(source,tarantulaContext.partitionNumber()).backup().set(key,value);
    }
    public int syncStart(String memberId,String source){
        RecoverService recoverService = scope==1?tarantulaContext.tarantulaCluster().recoverService():tarantulaContext.integrationCluster().recoverService();
        new Thread(()->{
            int[] total={0};
            long st = System.currentTimeMillis();
            if(!memberId.equals(nodeEngine.getLocalMember().getUuid())){
                int[] batch={0};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                this.tarantulaContext.dataStore(source,this.tarantulaContext.partitionNumber()).backup().list((k,v)->{
                    if(batch[0] == tarantulaContext.recoverBatchSize){
                        recoverService.sync(batch[0],keys,values,memberId,source);
                        batch[0] = 0;
                    }
                    keys[batch[0]]=k;
                    values[batch[0]]=v;
                    batch[0]++;
                    total[0]++;
                    return true;
                });
                //last batch
                recoverService.sync(batch[0],keys,values,memberId,source);
            }
            recoverService.syncEnd(memberId);
            log.warn("Total records ["+total[0]+"] from ["+source+"] synced to ["+memberId+"] timed (seconds) ["+((System.currentTimeMillis()-st)/1000)+"]");
        }).start();
        return this.tarantulaContext.partitionNumber();
    }
    public void replicateAsBatch(ReplicationData[] batch){
        for(ReplicationData d : batch){
            replicate(d.source,d.key,d.value);
        }
        total.addAndGet(batch.length);
    }
    public void syncEnd(){
        TarantulaContext._syc_finished.countDown();
        log.warn("Total records received ["+total.get()+"] from master node");
    }

    public boolean queryStart(String memberId,String source,String dataStore,int factoryId,int classId,String[] params){
        RecoverableFactory fac = this.tarantulaContext.recoverableRegistry(factoryId).query(classId,params);
        RecoverService recoverService = scope==1?tarantulaContext.tarantulaCluster().recoverService():tarantulaContext.integrationCluster().recoverService();
        new Thread(()->{
            this.tarantulaContext.dataStore(dataStore,tarantulaContext.partitionNumber()).backup().list(fac,(k,v)->{
                recoverService.query(memberId,source,k,v);
                return true;
            });
            recoverService.queryEnd(memberId,source);
        }).start();
        return fac!=null;
    }
    public void query(String source,byte[] key,byte[] value){
        this.deploymentServiceProvider.distributionCallback().queryCallback(source).on(key,value);
    }
    public void queryEnd(String source){
        this.deploymentServiceProvider.distributionCallback().queryEndCallback(source).on();
    }
}
