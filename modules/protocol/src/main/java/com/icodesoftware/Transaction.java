package com.icodesoftware;

public interface Transaction extends AutoCloseable{

    boolean execute(TransactionContext transactionContext);

    @Override
    void close();

    void register(DataStoreContext dataStoreContext, Listener listener);

    interface TransactionContext{
        boolean update(DataStoreContext dataStoreContext);
    }
    interface DataStoreContext extends Closable{
        default void parent(DataStoreContext parentContext){}
        default DataStore onDataStore(String name){ return null;}

    }
    interface Listener{
        default void afterCommit(long transactionId){}
        default void afterAbort(long transactionId,Exception exception){}
    }
    interface Log{
        boolean deleting();
        int scope();
        String source();
        String edgeLabel();
        long revisionNumber();
        byte[] key();
        byte[] value();
        byte[] edgeKey();

    }
}
