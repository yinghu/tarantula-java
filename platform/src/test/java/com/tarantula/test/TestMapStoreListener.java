package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.*;

public class TestMapStoreListener implements MapStoreListener {

    ServiceContext serviceContext;

    TransactionLogManager transactionLogManager;
     @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {
        transactionLogManager = new TransactionLogManager();
        transactionLogManager.setup(serviceContext);
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        transactionLogManager.onUpdating(metadata,key,value,transactionId);
    }

    @Override
    public void onCommit(int scope,long transactionId) {
         transactionLogManager.onCommit(scope,transactionId);
     }

    @Override
    public void onAbort(int scope,long transactionId) {
        transactionLogManager.onAbort(scope,transactionId);
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        return transactionLogManager.onRecovering(metadata,key,buffer);
    }

    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferEdgeStream bufferStream){
        return transactionLogManager.onRecovering(metadata,key,bufferStream);
    }


    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
         return transactionLogManager.onDeleting(metadata,key,value,transactionId);
    }
}
