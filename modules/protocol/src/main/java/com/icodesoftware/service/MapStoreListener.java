package com.icodesoftware.service;

import com.icodesoftware.Recoverable;


public interface MapStoreListener extends ServiceProvider {

    //dispatch backup operation
    <T extends Recoverable> void onBackingUp(Metadata metadata,String key,T t);

    //dispatch cluster operation
    void onDistributing(Metadata metadata,String stringKey, byte[] key, byte[] value);

    //recover cluster operation
    byte[] onRecovering(Metadata metadata,String stringKey,byte[] key);

    void onDeleting(Metadata metadata,byte[] key);



}
