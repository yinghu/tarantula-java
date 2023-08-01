package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.KeyIndexService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;

import java.util.Properties;

public class KeyIndexClusterService implements ManagedService, RemoteService {

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

    public KeyIndex setIfAbsent(String key,KeyIndex pending){
        logger.warn("set if absent ["+key+"]");
        pending.index(key);
        pending.disabled(true);
        return pending;
    }

    public void setup() throws Exception{
        TarantulaContext._integrationClusterStarted.await();
        //this.deploymentServiceProvider = this.tarantulaContext.deploymentServiceProvider();
        for(DataStoreOnPartition dso : dataStoreOnPartitions){
            dso.dataStore = this.tarantulaContext.dataStoreProvider().createKeyIndexDataStore(dso.name);
        }
        TarantulaContext._access_index_syc_finished.countDown();
        logger.warn("Key index service is ready on ["+nodeEngine.getLocalMember().getUuid()+"]");
    }
}
