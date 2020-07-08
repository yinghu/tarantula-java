package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;

import java.util.Map;

/**
 * Updated by yinghu lu on 7/4/2020
 */
public interface MapStoreListener {

    //call on create
    byte[] onCreating(Metadata metadata,String key,Map<String,Object> creating);
    //call on load
    byte[] onLoading(Metadata metadata,String key);

    byte[] onUpdating(Metadata metadata,String key,Map<String,Object> pending);

    //byte[] onBackup(Metadata metadata,String key,Map<String,Object> pending);

    //void onUpdated(Metadata metadata, byte[] key, byte[] value);
    //void onLoaded(Metadata metadata,byte[] key,byte[] value);
}
