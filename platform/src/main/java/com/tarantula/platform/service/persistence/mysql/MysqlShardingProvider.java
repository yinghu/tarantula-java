package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.Metadata;
import com.tarantula.platform.service.persistence.ShardingProvider;

import java.util.Map;

public class MysqlShardingProvider implements ShardingProvider {


    public byte[] onUpdating(Metadata metadata, String key, Map<String, Object> pending) {
        String ds = metadata.source();
        int pt = metadata.partition();
        return new byte[0];
    }


    public void onUpdated(Metadata metadata, byte[] key, byte[] value) {

    }


    public void onLoaded(Metadata metadata, byte[] key, byte[] value) {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
