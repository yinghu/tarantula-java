package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;


public interface MapStoreListener {

    //dispatch backup operations
    <T extends Recoverable> void onCreating(Metadata metadata, String key, T t);
    <T extends Recoverable> void onUpdating(Metadata metadata,String key,T t);

    //dispatch cluster operations
    void onDistributing(Metadata metadata,String stringKey, byte[] key, byte[] value);
    byte[] onRecovering(Metadata metadata,byte[] key);

}
