package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.TransactionReplicationEvent;

import java.util.List;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    public IntegrationScopeReplicationProxy(){
        super("integration");
    }

    @Override
    public void onCommit(int scope,long transactionId) {
        super.onCommit(scope,transactionId);
        ReplicationSynchronizerTimeout replicationEvent = new ReplicationSynchronizerTimeout(asyncInterval,()->{
            List<TransactionLog> logs = transactionLogManager.committed(scope,transactionId);
            TransactionReplicationEvent transactionReplicationEvent = new TransactionReplicationEvent();
            transactionReplicationEvent.destination(MapStoreListener.INTEGRATION_MAP_STORE_NAME);
            transactionReplicationEvent.pendingLogs = new PortableTransactionLog[logs.size()];
            for(int i=0;i<logs.size();i++){
                transactionReplicationEvent.pendingLogs[i]= new PortableTransactionLog(logs.get(i));
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
        byte[] fromCluster = serviceContext.clusterProvider().accessIndexService().onRecover(key.array());
        if(fromCluster==null) return false;
        key.rewind();
        for(byte b : fromCluster){
            buffer.writeByte(b);
        }
        buffer.flip();
        transactionLogManager.onUpdating(metadata,key,buffer,-1);
        key.rewind();
        buffer.clear();
        return super.onRecovering(metadata,key,buffer);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);
        super.setup(serviceContext);
    }
}
