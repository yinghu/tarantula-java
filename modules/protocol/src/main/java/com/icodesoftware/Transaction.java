package com.icodesoftware;

public interface Transaction extends Closable{

    boolean execute(TransactionContext transactionContext);

    void register(DataStoreContext dataStoreContext,Listener listener);
    interface TransactionContext{
        boolean update(DataStoreContext dataStoreContext);
    }
    interface DataStoreContext extends Closable{
        default void parent(DataStoreContext parentContext){}
        default DataStore onDataStore(String name){ return null;}
    }
    interface Listener{
        void afterCommit();
        void afterAbort(Exception exception);
    }
}
