package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;

import java.util.Map;

/**
 * Updated by yinghu lu on 7/4/2020
 */
public interface MapStoreListener {

    //return null if 

    //call before updating
    byte[] onCreating(Metadata metadata,String key,Map<String,Object> creating);
    byte[] onUpdating(Metadata metadata,String key,Map<String,Object> pending);
    //void onLoading()

    void onUpdated(Metadata metadata, byte[] key, byte[] value);
    void onLoaded(Metadata metadata,byte[] key,byte[] value);
}
