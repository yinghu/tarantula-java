package com.icodesoftware;

public interface Transaction extends Closable{

    boolean execute(TransactionContext transactionContext);

    void register(DataStoreContext dataStoreContext,Listener listener);
    interface TransactionContext{
        boolean update(DataStoreContext dataStoreContext);
    }
    interface DataStoreContext{
        void parent(DataStoreContext parentContext);
        DataStore onDataStore(String name);
    }
    interface Listener{
        void afterCommit();
        void afterAbort(Exception exception);
    }
}
