package com.icodesoftware.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;

public interface MapStoreListener extends ServiceProvider {

    //dispatch cluster operation
    void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId);
    //recover cluster operation
    boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value);

    boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferEdgeStream bufferStream);

    boolean onDeleting(Metadata metadata,Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId);

    void onCommit(int scope,long transactionId);
    void onAbort(int scope,long transactionId);

}
