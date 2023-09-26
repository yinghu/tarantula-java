package com.icodesoftware;

public interface Transaction extends Closable{

    void execute(TransactionContext transactionContext);

    void setup(DataStoreContext dataStoreContext);
    interface TransactionContext{
        boolean update(DataStoreContext dataStoreContext);
    }
    interface DataStoreContext{
        void parent(DataStoreContext parentContext);
        DataStore onDataStore(String name);
    }
}
