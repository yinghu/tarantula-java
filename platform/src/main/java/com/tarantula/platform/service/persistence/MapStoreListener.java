package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;


public interface MapStoreListener {

    //dispatch backup operations
    <T extends Recoverable> byte[] onCreating(Metadata metadata, String key, T t);
    <T extends Recoverable> byte[] onUpdating(Metadata metadata,String key,T t);
    <T extends Recoverable> T onLoading(Metadata metadata,String key);

    //dispatch cluster operations
    void onDistributing(Metadata metadata,byte[] key, byte[] value);
    byte[] onRecovering(Metadata metadata,byte[] key);
}
