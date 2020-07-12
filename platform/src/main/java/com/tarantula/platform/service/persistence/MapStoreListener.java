package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;
import com.tarantula.Recoverable;

import java.util.Map;

/**
 * Updated by yinghu lu on 7/4/2020
 * The data store operations dispatcher
 */
public interface MapStoreListener {

    //dispatch backup operations
    <T extends Recoverable> byte[] onCreating(Metadata metadata,String key,T t);
    <T extends Recoverable> byte[] onUpdating(Metadata metadata,String key,T t);
    byte[] onLoading(Metadata metadata,String key);

    //dispatch cluster operations
    void onDistributing(Metadata metadata,byte[] key, byte[] value);
    byte[] onRecovering(Metadata metadata,byte[] key);

    //backup partition version on backup
    int onVersioning(Metadata metadata);

}
