package com.tarantula.platform.service.persistence;

import com.tarantula.platform.service.Serviceable;

import java.util.Map;
/**
 * Created by yinghu on 7/5/2020.
 */
public interface ShardingProvider extends Serviceable {

    String name();
    int scope();
    void configure(Map<String,String> properties);
    void addShard(Shard shard);
}
