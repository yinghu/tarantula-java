package com.icodesoftware.lmdb.partition;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;

public class PartitionTransaction implements Transaction, Transaction.DataStoreContext, Transaction.Listener {

    private final int scope;
    private final LMDBPartitionProvider lmdbPartitionProvider;

    public PartitionTransaction(int scope,LMDBPartitionProvider lmdbPartitionProvider){
        this.scope = scope;
        this.lmdbPartitionProvider = lmdbPartitionProvider;
    }

    @Override
    public boolean execute(TransactionContext transactionContext) {
        transactionContext.update(this);
        return true;
    }

    @Override
    public DataStore onDataStore(String name) {
        return lmdbPartitionProvider.createDataStore(scope,name);
    }

    @Override
    public void close() {

    }

    @Override
    public void register(DataStoreContext dataStoreContext, Listener listener) {

    }
}
