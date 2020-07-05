package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;

import java.util.Map;

/**
 * Updated by yinghu lu on 4/7/2019.
 */
public interface MapStoreListener {

    void onUpdating(Map<String,Object> pending);
    //void onLoading()

    void onUpdated(Metadata metadata, byte[] key, byte[] value);
    void onLoaded(Metadata metadata,byte[] key,byte[] value);
}
