package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
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
        System.out.println("PTX : "+txn.getId()+" : " + txn.getParent());
        try{
            transactionContext.update(this);
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
        System.out.println("Close transaction!");
    }

    @Override
    public DataStore onDataStore(String name) {
        //Dbi dbi1 = dataStoreProvider.data.openDbi(txn,name.getBytes(),null, DbiFlags.MDB_CREATE);
        LMDBDataStore dataStore = dataStoreProvider.createDataStore(scope,name,txn);
        //Txn<ByteBuffer> c = dataStoreProvider.data.txn(txn);
        //System.out.println("TXN 0 : "+c.getId()+" : " + c.getParent().getId());
        return new LocalDataStore(txn,dataStore);
    }
}
