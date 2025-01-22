package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.LocalMetadata;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.event.TransactionReplicationEvent;
import com.tarantula.platform.service.cluster.ClusterBatch;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.TarantulaContext;

import java.util.List;
import java.util.Properties;


public class ClusterRecoverService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(ClusterRecoverService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    private int scope;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.scope = Integer.parseInt(properties.getProperty("tarantula-scope"));
        tarantulaContext = TarantulaContext.getInstance();
        new ServiceBootstrap(TarantulaContext._integrationClusterStarted,TarantulaContext._recoverServiceStarted,new RecoverServiceBootstrap(this),"recover-service",true).start();
        log.info("Cluster Recover Service Started on scope ["+scope+"]");
    }

    public void setup() throws Exception{
        this.tarantulaContext.clusterProvider().subscribe(MapStoreListener.DATA_MAP_STORE_NAME, event -> {
            if(event.source().equals(tarantulaContext.node().nodeName())) return false;
            if(event instanceof TransactionReplicationEvent){
                tarantulaContext.onTransactionEvent(Distributable.DATA_SCOPE,(TransactionReplicationEvent)event);
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
    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new RecoverServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public void delete(String source,byte[] key){
        DataStore dataStore = this.tarantulaContext.deploymentDataStoreProvider.createDataStore(source);
        dataStore.backup().unset((k,v)->{
            for(byte b: key){
                k.writeByte(b);
            }
            return true;
        });
    }
    public void deleteEdge(String source,String label,byte[] key){
        DataStore dataStore = this.tarantulaContext.deploymentDataStoreProvider.createDataStore(source);
        dataStore.backup().unsetEdge(label,(k,v)->{
            for(byte b: key){
                k.writeByte(b);
            }
            return true;
        },true);
    }
    public void deleteEdge(String source,String label,byte[] key,byte[] edge){
        DataStore dataStore = this.tarantulaContext.deploymentDataStoreProvider.createDataStore(source);
        dataStore.backup().unsetEdge(label,(k,v)->{
            for(byte b: key){
                k.writeByte(b);
            }
            for(byte b: edge){
                v.writeByte(b);
            }
            return true;
        },false);
    }
    public byte[] load(String source,byte[] key){
        TransactionLogManager transactionLogManager = this.tarantulaContext.transactionLogManager(Distributable.DATA_SCOPE);
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE, source);
        return transactionLogManager.loadFromCommitted(metadata,key);
    }

    public ClusterBatch loadEdgeValueSet(String source, String label, byte[] key){
        TransactionLogManager transactionLogManager = this.tarantulaContext.transactionLogManager(Distributable.DATA_SCOPE);
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE, source,label);
        List<Batchable.BatchData> batchable = transactionLogManager.loadEdgeValueFromCommitted(metadata,key);
        return new ClusterBatch(batchable);
    }


    public String[] onListModules(){
        return this.tarantulaContext._listModuleContent();
    }
    public byte[] onLoadModuleJarFile(String fileName){
        return this.tarantulaContext._readContent(fileName);
    }

    public void replicate(TransactionReplicationEvent transactionReplicationEvent){
        tarantulaContext.onTransactionEvent(Distributable.DATA_SCOPE,transactionReplicationEvent);
    }


}
