package com.tarantula.platform.service.persistence;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by yinghu on 7/5/2020.
 */
public class Shard {
    final public int shardNumber;
    public Map<String,String> properties;

    public Shard(int shardNumber){
        this.shardNumber = shardNumber;
        properties = new HashMap<>();
    }
}
