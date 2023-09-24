package com.icodesoftware;

public interface Transaction extends Closable{

    void execute(TransactionContext transactionContext);

    interface TransactionContext{
        boolean update(DataStoreContext dataStoreContext);
    }
    interface DataStoreContext{
        DataStore onDataStore(String name);
    }
}
