package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;
import org.lmdbjava.Txn;
import java.nio.ByteBuffer;

public class LocalTransaction implements Transaction, Transaction.DataStoreContext, Transaction.Listener {

    private final LMDBDataStoreProvider dataStoreProvider;
    private final int scope;

    private Txn<ByteBuffer> txn;
    private DataStoreContext dataStoreContext;
    private Listener listener;
    public LocalTransaction(int scope,LMDBDataStoreProvider dataStoreProvider){
        this.scope = scope;
        this.dataStoreProvider = dataStoreProvider;
        this.dataStoreContext = this;
        this.listener = this;
    }
    @Override
    public boolean execute(TransactionContext transactionContext) {
        txn = dataStoreProvider.txn(scope);
        try{
            if(!transactionContext.update(this.dataStoreContext)){
                txn.abort();
                listener.afterAbort(null);
                return false;
            }
            txn.commit();
            listener.afterCommit();
            return true;
        }catch (Exception ex){
            listener.afterAbort(ex);
            txn.abort();
            return false;
        }
        finally {
            txn.close();
        }
    }

    @Override
    public void close() {
        //clear transaction resources
    }

    @Override
    public DataStore onDataStore(String name) {
       return dataStoreProvider.createDataStore(scope,name,txn);
    }

    public void register(DataStoreContext dataStoreContext,Listener listener){
        if(dataStoreContext==null || listener==null) throw new IllegalArgumentException("not null required");
        this.dataStoreContext = dataStoreContext;
        this.dataStoreContext.parent(this);
        this.listener = listener;
    }

    public void parent(DataStoreContext parentContext){

    }

    @Override
    public void afterCommit() {

    }

    @Override
    public void afterAbort(Exception exception) {

    }
}
