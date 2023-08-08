package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.Event;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.KeyIndexService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;

import java.util.Properties;

public class KeyIndexClusterService implements ManagedService, RemoteService,KeyIndexService {

    private static TarantulaLogger logger = JDKLogger.getLogger(KeyIndexClusterService.class);
    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    private DataStoreOnPartition[] dataStoreOnPartitions;


    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        tarantulaContext = TarantulaContext.getInstance();
        this.nodeEngine = nodeEngine;
        this.dataStoreOnPartitions = new DataStoreOnPartition[this.nodeEngine.getPartitionService().getPartitionCount()];
        for(int i=0;i<this.dataStoreOnPartitions.length;i++){
            this.dataStoreOnPartitions[i]=new DataStoreOnPartition(i, KeyIndexService.KeyIndexStore.STORE_NAME_PREFIX +i);
        }
        new ServiceBootstrap(tarantulaContext._storageStarted,tarantulaContext._accessIndexServiceStarted,new KeyIndexServiceBootstrap(this),"key-index-service",true).start();
    }

    public byte[] get(int partition,byte[] key) {
        DataStoreOnPartition dso = onPartition(partition);
        return dso.dataStore.backup().get(key);
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
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStoreProvider().createKeyIndexDataStore(dso.name);
        }
        tarantulaContext.clusterProvider().subscribe(KeyIndexService.NAME,event -> {
            KeyIndex keyIndex = new KeyIndexTrack();
            keyIndex.owner(event.owner());
            keyIndex.index(event.index());
            String ckey = keyIndex.key().asString();
            DataStoreOnPartition dso = onPartition(ckey);
            byte[] key = ckey.getBytes();
            return dso.lock(key,()->{
                if(dso.dataStore.load(keyIndex)){
                    if(keyIndex.placeMasterNode(event.source()) || keyIndex.placeSlaveNode(event.label())){
                        dso.dataStore.update(keyIndex);
                    }
                    return true;
                }
                keyIndex.placeMasterNode(event.source());
                keyIndex.placeSlaveNode(event.label());
                return dso.dataStore.createIfAbsent(keyIndex,false);
            });
        });
        tarantulaContext.keyIndexService = this;
        TarantulaContext._access_index_syc_finished.countDown();
        logger.warn("Key index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]");
    }

    @Override
    public KeyIndex lookup(String source,String key) {
        DataStoreOnPartition dso = onPartition(key);
        KeyIndexTrack keyIndexTrack = new KeyIndexTrack();
        keyIndexTrack.owner(source);
        keyIndexTrack.index(key);
        if(dso.lock(key.getBytes(),()-> dso.dataStore.load(keyIndexTrack))) return keyIndexTrack;
        return null;
    }

    public ClusterProvider.Node[] recoveringNodeList(String source, String key){
        return null;
    }

    public void onReplicated(Event event){
        this.tarantulaContext.clusterProvider().publisher().publish(event);
    }

    public boolean createIfAbsent(KeyIndex keyIndex){
        String key = keyIndex.key().asString();
        DataStoreOnPartition dso = onPartition(key);
        return dso.lock(key.getBytes(),()-> dso.dataStore.createIfAbsent(keyIndex,true));
    }

    public boolean update(KeyIndex keyIndex){
        String key = keyIndex.key().asString();
        DataStoreOnPartition dso = onPartition(key);
        return dso.lock(key.getBytes(),()-> dso.dataStore.update(keyIndex));
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
}
