package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceProvider;

import java.util.Map;
/**
 * updated by yinghu on 7/18/2020.
 */
public interface ShardingProvider extends ServiceProvider {

    boolean enabled();
    int scope();
    void configure(Map<String,String> properties);
    void addShard(Shard shard);
    void registerDataStore(String name);
    void registerDataStore(String prefix,int partitions);

    <T extends Recoverable> T load(Metadata metadata, String key);

    <T extends Recoverable> byte[] update(Metadata metadata, String key, T t);
    <T extends Recoverable> byte[] create(Metadata metadata, String key, T t);

}
