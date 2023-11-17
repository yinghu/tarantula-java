package com.tarantula.platform.service.persistence;


import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.TransactionReplicationEvent;


import java.util.List;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);

    public IntegrationScopeReplicationProxy(){
        super(Distributable.INTEGRATION_SCOPE);
    }

    @Override
    public void onCommit(int scope,long transactionId) {
        super.onCommit(scope,transactionId);
        serviceContext.schedule(new ReplicationSynchronizerTimeout(()->{
            List<TransactionLog> logs = transactionLogManager.committed(scope,transactionId);
            TransactionReplicationEvent transactionReplicationEvent = new TransactionReplicationEvent();
            transactionReplicationEvent.destination(MapStoreListener.INTEGRATION_MAP_STORE_NAME);
            transactionReplicationEvent.pendingLogs = new PortableTransactionLog[logs.size()];
            for(int i=0;i<logs.size();i++){
                transactionReplicationEvent.pendingLogs[i]= new PortableTransactionLog(logs.get(i));
            }
            serviceContext.clusterProvider().publisher().publish(transactionReplicationEvent);
        }));
    }
}
