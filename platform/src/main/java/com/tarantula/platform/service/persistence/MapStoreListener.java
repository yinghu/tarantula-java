package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;
import com.tarantula.Recoverable;

import java.util.Map;

/**
 * Updated by yinghu lu on 7/4/2020
 */
public interface MapStoreListener {


    <T extends Recoverable> byte[] onCreating(Metadata metadata,String key,T t);
    <T extends Recoverable> byte[] onUpdating(Metadata metadata,String key,T t);
    byte[] onLoading(Metadata metadata,String key);


    void onDistributing(Metadata metadata,byte[] key, byte[] value);
    byte[] onRecovering(Metadata metadata,byte[] key);

    int onVersioning(Metadata metadata);

}
