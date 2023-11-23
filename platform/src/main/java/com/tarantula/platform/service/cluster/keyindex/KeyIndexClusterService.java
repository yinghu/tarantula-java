package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.event.KeyIndexEvent;
import com.tarantula.platform.service.KeyIndexTrack;

import java.util.Properties;

public class KeyIndexClusterService implements ManagedService, RemoteService,KeyIndexService {

    private static TarantulaLogger logger = JDKLogger.getLogger(KeyIndexClusterService.class);
    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;


    private ClusterNodeManager clusterNodeManager;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        tarantulaContext = TarantulaContext.getInstance();
        this.nodeEngine = nodeEngine;
        clusterNodeManager = new ClusterNodeManager(tarantulaContext.node());
        tarantulaContext.clusterProvider().registerNodeListener(clusterNodeManager);
        new ServiceBootstrap(TarantulaContext._integrationClusterStarted,TarantulaContext._keyIndexServiceStarted,new KeyIndexServiceBootstrap(this),"key-index-service",true).start();
    }

    public byte[] get(String source,byte[] key) {
        DataStore dso = this.tarantulaContext.deploymentDataStoreProvider.createKeyIndexDataStore(source);
        logger.warn("SRC : "+source+" DB: "+dso.name());
        byte[][] data={null};
        if(!dso.backup().get(new BinaryKey(key),(k,v)->{
            data[0]=v.array();
            return true;
        })) return null;
        return data[0];
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new KeyIndexServiceProxy(objectName,this.nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public void setup() throws Exception{
        TarantulaContext._integrationClusterStarted.await();
        tarantulaContext.clusterProvider().subscribe(KeyIndexService.NAME,event -> {
            KeyIndexEvent keyIndexEvent = (KeyIndexEvent)event;
            KeyIndex keyIndex = new KeyIndexTrack(keyIndexEvent.owner(),new BinaryKey(keyIndexEvent.payload()));
            DataStore dataStore = this.dataStore(keyIndex.owner());
            keyIndex.placeMasterNode(keyIndexEvent.source());
            keyIndex.placeSlaveNode(keyIndexEvent.label());
            if(!dataStore.createIfAbsent(keyIndex,true)){
                keyIndex.placeMasterNode(keyIndexEvent.source());
                keyIndex.placeSlaveNode(keyIndexEvent.label());
                dataStore.update(keyIndex);
            }
            return true;
        });
        tarantulaContext.keyIndexService = this;
        TarantulaContext._cluster_service_ready.countDown();
        logger.warn("Key index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]");
    }


    public KeyIndex lookup(String source, Recoverable.Key key){
        KeyIndexTrack keyIndexTrack = new KeyIndexTrack(source,key);
        DataStore dataStore = dataStore(source);
        if(dataStore.load(keyIndexTrack)) return keyIndexTrack;
        return null;
    }

    public ClusterProvider.Node[] recoveringNodeList(String source, String key){
       return null;
    }

    public void onReplicated(Event event){
        this.tarantulaContext.clusterProvider().publisher().publish(event);
    }

    public boolean createIfAbsent(KeyIndex keyIndex){
        DataStore dso = this.dataStore(keyIndex.owner());
        return dso.createIfAbsent(keyIndex,true);
    }

    public boolean update(KeyIndex keyIndex){
        DataStore dso = this.dataStore(keyIndex.owner());
        return dso.update(keyIndex);
    }

    public ClusterProvider.Node nextNode(){
        return clusterNodeManager.nextNode();
    }
    public ClusterProvider.Node[] nextNodeList(int expected){
        return clusterNodeManager.nextNodeList(expected);
    }

    public ClusterProvider.Node[] nodeList(KeyIndex keyIndex){
        return clusterNodeManager.nodeList(keyIndex);
    }

    public ClusterProvider.Node[] nodeList(KeyIndex keyIndex,int expected){
        return clusterNodeManager.nodeList(keyIndex,tarantulaContext.clusterProvider().maxReplicationNumber());
    }


    @Override
    public String name() {
        return KeyIndexService.NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


    public boolean startSync(String memberId,String syncKey){
        DistributionKeyIndexService distributionKeyIndexService = this.tarantulaContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
        new Thread(()->{
            this.tarantulaContext.deploymentDataStoreProvider.list(Distributable.INDEX_SCOPE).forEach(db->{
                DataStore dataStore = this.tarantulaContext.deploymentDataStoreProvider.lookup(db);
                //logger.warn("DB :"+db+" Count : "+dataStore.count());
                int[] batch ={0};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                dataStore.backup().forEach((k,v)->{
                    if(batch[0]==tarantulaContext.recoverBatchSize){
                        distributionKeyIndexService.onSync(batch[0],keys,values,memberId,db);
                        batch[0]=0;
                    }
                    keys[batch[0]]=k.array();
                    values[batch[0]]=v.array();
                    batch[0]++;
                    return true;
                });
                if(batch[0]>0) distributionKeyIndexService.onSync(batch[0],keys,values,memberId,db);
            });
            distributionKeyIndexService.endSync(memberId,syncKey);
        }).start();
        return true;
    }
    public boolean endSync(String syncKey){
        //tarantulaContext._syncLatch.get(syncKey).countDown();
        return true;
    }

    public void sync(byte[][] keys,byte[][] values,String source){
        logger.warn("DB :"+source+" Batch : "+keys.length);
        DataStore dso = this.tarantulaContext.deploymentDataStoreProvider.createKeyIndexDataStore(source);
        for(int i=0;i<keys.length;i++){
            final int ix = i;
            dso.backup().set((k,v)->{
                for(byte b : keys[ix]){
                    k.writeByte(b);
                }
                for(byte b : values[ix]){
                    v.writeByte(b);
                }
                return true;
            });
        }
    }

    private DataStore dataStore(String source){
        return this.tarantulaContext.deploymentDataStoreProvider.createKeyIndexDataStore(KeyIndexService.STORE_NAME+source);
    }

}
