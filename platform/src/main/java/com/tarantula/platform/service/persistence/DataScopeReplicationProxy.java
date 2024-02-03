package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.logging.JDKLogger;
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
        logger.warn(name()+ " : Recovery : "+recovery);
        return recovery;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);
        super.setup(serviceContext);
    }
}


