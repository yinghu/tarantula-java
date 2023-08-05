package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;


public interface MapStoreListener {

    //dispatch backup operation
    <T extends Recoverable> void onBackingUp(Metadata metadata,String key,T t);

    //dispatch cluster operation
    void onDistributing(Metadata metadata,String stringKey, byte[] key, byte[] value);
    void onDistributing(Metadata metadata,String stringKey, byte[] key, RevisionObject value);
    //recover cluster operation
    byte[] onRecovering(Metadata metadata,byte[] key);

    void onDeleting(Metadata metadata,byte[] key);


}
