package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.TransactionReplicationEvent;
import com.tarantula.platform.service.cluster.DistributionReplicator;

import java.util.List;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    public IntegrationScopeReplicationProxy(){
        super("integration", Distributable.INTEGRATION_SCOPE);
    }

    @Override
    public void onCommit(int scope,long transactionId) {
        super.onCommit(scope,transactionId);
        ReplicationSynchronizerTimeout replicationEvent = new ReplicationSynchronizerTimeout(asyncInterval,()->{
            List<TransactionLog> logs = transactionLogManager.committed(scope,transactionId);
            transactionLogManager.onTransaction(logs);//local index
            TransactionReplicationEvent transactionReplicationEvent = new TransactionReplicationEvent();
            transactionReplicationEvent.destination(MapStoreListener.INTEGRATION_MAP_STORE_NAME);
            transactionReplicationEvent.pendingLogs = new PortableTransactionLog[logs.size()];
            for(int i=0;i<logs.size();i++){
                transactionReplicationEvent.pendingLogs[i]= new PortableTransactionLog(logs.get(i));
            }
            if(broadcasting) {
                distributionReplicator.replicate(transactionReplicationEvent);
                return;
            }
            distributionReplicator.replicate(transactionReplicationEvent,maxReplicationNodes);
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
        byte[] fromCluster = serviceContext.clusterProvider().accessIndexService().onRecover(akey);
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
    public void setup(ServiceContext serviceContext) {
        logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);
        super.setup(serviceContext);
        super.transactionLogManager.registerTransactionListener(this);
    }
}
