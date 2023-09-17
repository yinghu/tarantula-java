package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface MapStoreListener extends ServiceProvider {

    //dispatch backup operation
    //<T extends Recoverable> void onBackingUp(Metadata metadata,String key,T t);

    //dispatch cluster operation
    void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value);
    //recover cluster operation
    boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value);

    void onDeleting(Metadata metadata,Recoverable.DataBuffer key);

}
