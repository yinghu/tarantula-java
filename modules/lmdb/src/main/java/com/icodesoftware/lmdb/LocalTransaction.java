package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;
import org.lmdbjava.Txn;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LocalTransaction implements Transaction, Transaction.DataStoreContext {

    private final LMDBDataStoreProvider dataStoreProvider;
    private final int scope;

    private Txn<ByteBuffer> txn;
    private ArrayList<DataStore> joined = new ArrayList<>();
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
        joined.forEach(db->db.close());
        joined.clear();
    }

    @Override
    public DataStore onDataStore(String name) {
        DataStore dataStore = dataStoreProvider.createDataStore(scope,name,txn);
        joined.add(dataStore);
        return dataStore;
    }
}
