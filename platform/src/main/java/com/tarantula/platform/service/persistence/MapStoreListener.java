package com.tarantula.platform.service.persistence;

import com.tarantula.Metadata;

/**
 * Updated by yinghu lu on 4/7/2019.
 */
public interface MapStoreListener {
    void onUpdated(Metadata metadata, byte[] key, byte[] value);
    void onLoaded(Metadata metadata,byte[] key,byte[] value);
}
