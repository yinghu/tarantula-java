package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Transaction;

import java.lang.foreign.Arena;

public class NativeLocalTransaction implements Transaction, Transaction.DataStoreContext, Transaction.Listener{

    private NativeDataStoreProvider nativeDataStoreProvider;
    private NativeEnv nativeEnv;
    private final NativeTxn txn;
    private final long transactionId;
    private Arena arena = Arena.ofConfined();
    public NativeLocalTransaction(NativeEnv nativeEnv,NativeDataStoreProvider nativeDataStoreProvider){
        this.nativeEnv = nativeEnv;
        this.nativeDataStoreProvider = nativeDataStoreProvider;
        txn = this.nativeEnv.write(arena);
        transactionId = txn.transactionId();
    }

    @Override
    public boolean execute(TransactionContext transactionContext) {
        try(txn){
            if(!transactionContext.update(this)){
                txn.abort();
                nativeDataStoreProvider.onAbort(nativeEnv.scope(),transactionId);
                return false;
            }
            txn.commit();
            nativeDataStoreProvider.onCommit(nativeEnv.scope(),transactionId);
            return true;
        }catch (Exception ex){
            txn.abort();
            return false;
        }
    }

    @Override
    public void close() {
        arena.close();
    }

    @Override
    public void register(DataStoreContext dataStoreContext, Listener listener) {

    }

    @Override
    public void parent(DataStoreContext parentContext) {

    }

    @Override
    public DataStore onDataStore(String name) {
        return new NativeDataStore(name,nativeDataStoreProvider,nativeEnv,txn);
    }
}
