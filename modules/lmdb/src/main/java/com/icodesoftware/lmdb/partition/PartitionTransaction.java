package com.icodesoftware.lmdb.partition;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;

public class PartitionTransaction implements Transaction, Transaction.DataStoreContext, Transaction.Listener {

    private final int scope;
    private final LMDBPartitionProvider lmdbPartitionProvider;
    private DataStoreContext dataStoreContext;
    private Listener listener;
    private final PartitionTxn txn;
    public PartitionTransaction(int scope,LMDBPartitionProvider lmdbPartitionProvider){
        this.scope = scope;
        this.lmdbPartitionProvider = lmdbPartitionProvider;
        this.dataStoreContext = this;
        txn = new PartitionTxn(lmdbPartitionProvider);
    }

    @Override
    public boolean execute(TransactionContext transactionContext) {
        try(txn){
            if(!transactionContext.update(this.dataStoreContext)){
                txn.abort();
                this.lmdbPartitionProvider.onAbort(scope, txn.transactionId());
                this.afterAbort(txn.transactionId(),null);
                return false;
            }
            txn.commit();
            this.lmdbPartitionProvider.onCommit(scope,txn.transactionId());
            this.afterCommit(txn.transactionId());
            return true;
        }catch (Exception ex){
            txn.abort();
            this.lmdbPartitionProvider.onAbort(scope,txn.transactionId());
            this.afterAbort(txn.transactionId(),ex);
            return false;
        }
    }

    @Override
    public DataStore onDataStore(String name) {
        return lmdbPartitionProvider.createDataStore(scope,name);
    }

    @Override
    public void close() {
        if(dataStoreContext==this) return;
        dataStoreContext.close();
    }

    @Override
    public void register(DataStoreContext dataStoreContext, Listener listener) {
        if(dataStoreContext==null || listener==null) throw new IllegalArgumentException("not null required");
        this.dataStoreContext = dataStoreContext;
        this.dataStoreContext.parent(this);
        this.listener = listener;
    }
    @Override
    public void afterCommit(long transactionId) {
        if(listener==null) return;
        listener.afterCommit(transactionId);
    }

    @Override
    public void afterAbort(long transactionId,Exception exception) {
        if(listener==null) return;
        listener.afterAbort(transactionId,exception);
    }
}
