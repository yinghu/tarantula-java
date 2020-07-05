package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.Metadata;
import com.tarantula.platform.service.persistence.MapStoreListener;

import java.util.Map;

public class MysqlShardingProvider implements MapStoreListener {

    @Override
    public byte[] onUpdating(Metadata metadata, String key, Map<String, Object> pending) {
        String ds = metadata.source();
        int pt = metadata.partition();
        return new byte[0];
    }

    @Override
    public void onUpdated(Metadata metadata, byte[] key, byte[] value) {

    }

    @Override
    public void onLoaded(Metadata metadata, byte[] key, byte[] value) {

    }
}
