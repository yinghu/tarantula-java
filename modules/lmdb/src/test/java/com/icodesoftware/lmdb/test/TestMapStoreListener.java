package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;


public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;
    TransactionLogManager transactionLogManager;

    TestVerifier verifier;
    TestVerifier abort;
    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
        transactionLogManager = new TransactionLogManager();
        TestContext context = new TestContext();
        context.lmdbDataStoreProvider = provider;
        transactionLogManager.setup(context);
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        return transactionLogManager.onRecovering(metadata,key,value);
    }
    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        return transactionLogManager.onRecovering(metadata,key,bufferStream);
    }
    @Override
    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        transactionLogManager.onUpdating(metadata,key,value,transactionId);
    }

    @Override
    public void onCommit(int scope,long transactionId) {
        transactionLogManager.onCommit(scope,transactionId);
        if(verifier==null) return;
        verifier.onTransaction(transactionId);
    }

    @Override
    public void onAbort(int scope,long transactionId) {
        transactionLogManager.onAbort(scope,transactionId);
        if(abort==null) return;
        abort.onTransaction(transactionId);
    }

    @Override
    public boolean onDeleting(Metadata metadata,Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        return transactionLogManager.onDeleting(metadata,key,value,transactionId);
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
