package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;
import com.tarantula.Recoverable;

import java.util.Map;

/**
 * Updated by yinghu lu on 7/4/2020
 */
public interface MapStoreListener {

    //call on create
    byte[] onCreating(Metadata metadata,String key,Map<String,Object> creating);

    <T extends Recoverable> byte[] onCreating(Metadata metadata,String key,T t);

    //call on load
    byte[] onLoading(Metadata metadata,String key);

    byte[] onUpdating(Metadata metadata,String key,Map<String,Object> pending);

    void onDistributing(Metadata metadata,byte[] key, byte[] value);

    int onVersioning(Metadata metadata);

}
