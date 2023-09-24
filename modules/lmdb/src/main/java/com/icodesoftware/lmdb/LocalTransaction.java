package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;
import org.lmdbjava.Txn;
import java.nio.ByteBuffer;

public class LocalTransaction implements Transaction, Transaction.DataStoreContext {

    private final LMDBDataStoreProvider dataStoreProvider;
    private final int scope;

    private Txn<ByteBuffer> txn;

    public LocalTransaction(int scope,LMDBDataStoreProvider dataStoreProvider){
        this.scope = scope;
        this.dataStoreProvider = dataStoreProvider;
    }
    @Override
    public void execute(TransactionContext transactionContext) {
        txn = dataStoreProvider.txn(scope);
        try{
            if(!transactionContext.update(this)){
                txn.abort();
                return;
            }
            txn.commit();
        }catch (Exception ex){
            ex.printStackTrace();
            txn.abort();
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
}
