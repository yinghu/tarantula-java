package com.tarantula.platform.service.persistence;

import com.tarantula.BucketListener;
import com.tarantula.Metadata;
import com.tarantula.Recoverable;
import com.tarantula.platform.service.Serviceable;

import java.util.Map;
/**
 * Created by yinghu on 7/5/2020.
 */
public interface ShardingProvider extends Serviceable, BucketListener {

    String name();
    int scope();
    void configure(Map<String,String> properties);
    void addShard(Shard shard);
    void registerDataStore(String name);
    void registerDataStore(String prefix,int partitions);

    //byte[] create(Metadata metadata,String key,Map<String,Object> data);
    byte[] load(Metadata metadata,String key);
    byte[] update(Metadata metadata,String key,Map<String,Object> data);

    <T extends Recoverable> byte[] update(Metadata metadata, String key, T t);
    <T extends Recoverable> byte[] create(Metadata metadata, String key, T t);
    //int version(int bucket);
}
