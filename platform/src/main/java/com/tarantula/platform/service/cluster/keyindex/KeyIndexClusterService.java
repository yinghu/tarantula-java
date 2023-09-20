package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.DataStore;
import com.icodesoftware.Event;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
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
        logger.warn("recovering data from ["+source+"]");
        DataStore dso = dataStore(source);
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
            for(int i=0;i<keyIndexEvent.owners.length;i++){
                KeyIndex keyIndex = new KeyIndexTrack();
                keyIndex.owner(keyIndexEvent.owners[i]);
                keyIndex.index(keyIndexEvent.keys[i]);
                String ckey = keyIndex.key().asString();
                DataStore dso = dataStore(event.source());
                byte[] key = ckey.getBytes();
                /**
                dso.lock(key,()->{
                    if(dso.dataStore.load(keyIndex)){
                        if(keyIndex.placeMasterNode(event.source()) || keyIndex.placeSlaveNode(event.label())){
                            dso.dataStore.update(keyIndex);
                        }
                        return true;
                    }
                    keyIndex.placeMasterNode(event.source());
                    keyIndex.placeSlaveNode(event.label());
                    return dso.dataStore.createIfAbsent(keyIndex,false);
                });**/
            }
            return true;
        });
        tarantulaContext.keyIndexService = this;
        TarantulaContext._cluster_service_ready.countDown();
        logger.warn("Key index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]");
    }


    public KeyIndex lookup(String source, Recoverable.Key key){
        logger.warn("lookup from ["+source+"]");
        KeyIndexTrack keyIndexTrack = new KeyIndexTrack(source,key);
        DataStore dataStore = tarantulaContext.deploymentDataStoreProvider.createKeyIndexDataStore(KeyIndexService.KeyIndexStore.STORE_NAME+source);
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
            for(int i=0;i<tarantulaContext.accessIndexRoutingNumber;i++){
                //DataStoreOnPartition dso = onPartition(i);
                int[] batch={0,i};
                byte[][] keys = new byte[tarantulaContext.recoverBatchSize][];
                byte[][] values = new byte[tarantulaContext.recoverBatchSize][];
                //dso.dataStore.backup().forEach((k,v)->{
                    //if(batch[0] == tarantulaContext.recoverBatchSize){
                        //distributionKeyIndexService.onSync(batch[0],keys,values,memberId,batch[1]);
                      //  batch[0] = 0;
                    //}
                    //keys[batch[0]]=k;
                    //values[batch[0]]=v;
                    //batch[0]++;
                    //total[0]++;
                    //return true;
                //});
                if(batch[0]>0) distributionKeyIndexService.onSync(batch[0],keys,values,memberId,batch[1]);
            }
            distributionKeyIndexService.endSync(memberId,syncKey);
        }).start();
        return true;
    }
    public boolean endSync(String syncKey){
        tarantulaContext._syncLatch.get(syncKey).countDown();
        return true;
    }

    public void sync(byte[][] keys,byte[][] values,int partition){
        for(int i=0;i<keys.length;i++){
            //DataStore dso = onPartition(partition);
            //int ix = i;
            //dso.lock(keys[i],()->dso.dataStore.backup().set(keys[ix],values[ix]));
        }
    }

    private DataStore dataStore(String source){
        return this.tarantulaContext.deploymentDataStoreProvider.createKeyIndexDataStore(KeyIndexStore.STORE_NAME+source);
    }

}
