package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Batchable;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.event.TransactionReplicationEvent;

import java.util.List;

public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    public DataScopeReplicationProxy(){
        super("data");
    }
    @Override
    public void onCommit(int scope,long transactionId) {
        super.onCommit(scope, transactionId);
        ReplicationSynchronizerTimeout replicationEvent = new ReplicationSynchronizerTimeout(asyncInterval, () -> {
            List<TransactionLog> logs = transactionLogManager.committed(scope, transactionId);
            transactionLogManager.onTransaction(logs);
            TransactionReplicationEvent transactionReplicationEvent = new TransactionReplicationEvent();
            transactionReplicationEvent.destination(MapStoreListener.DATA_MAP_STORE_NAME);
            transactionReplicationEvent.pendingLogs = new PortableTransactionLog[logs.size()];
            for (int i = 0; i < logs.size(); i++) {
                transactionReplicationEvent.pendingLogs[i] = new PortableTransactionLog(logs.get(i));
            }
            serviceContext.clusterProvider().publisher().publish(transactionReplicationEvent);
        });
        if (!asyncDistributing) {
            replicationEvent.run();
            return;
        }
        serviceContext.schedule(replicationEvent);
    }
    @Override
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer) {
        boolean recovery = super.onRecovering(metadata, key, buffer);
        if(recovery) return true;
        key.rewind();
        byte[] akey = key.array();
        byte[] fromCluster = serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),akey);
        if(fromCluster==null) return false;
        DataStore dataStore = transactionLogManager().onTransaction(metadata);
        dataStore.backup().set((k,v)->{
            for(byte b : akey){
                k.writeByte(b);
            }
            for(byte b : fromCluster){
                v.writeByte(b);
            }
            return true;
        });
        key.rewind();
        buffer.clear();
        return super.onRecovering(metadata,key,buffer);
    }

    @Override
    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        boolean recovery = super.onRecovering(metadata,key,bufferStream);
        if(recovery) return true;
        if(metadata.label()==null) return false;
        key.rewind();
        byte[] akey = key.array();
        Batchable fromCluster = serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),metadata.label(),akey);
        if(fromCluster==null) {
            //logger.warn("No data from cluster");
            return false;
        }
        int sz = fromCluster.size();
        //logger.warn("Data from cluster size : "+sz);
        Batchable.BatchData[] batch = fromCluster.batch();
        DataStore dataStore = transactionLogManager().onTransaction(metadata);
        for(int i=0;i<sz;i++){
            Batchable.BatchData kv = batch[i];
            byte[] ak = kv.key();
            byte[] av = kv.value();
            dataStore.backup().set((k,v)->{
                for(byte b : ak){
                    k.writeByte(b);
                }
                for(byte b : av){
                    v.writeByte(b);
                }
                return true;
            });
            dataStore.backup().setEdge(metadata.label(),(k,v)->{
                for(byte b : akey){
                    k.writeByte(b);
                }
                for(byte b : ak){
                    v.writeByte(b);
                }
                return true;
            });
        }
        key.rewind();
        return super.onRecovering(metadata,key,bufferStream);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);
        super.setup(serviceContext);
    }
}


