package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.persistence.Shard;
import com.tarantula.platform.service.persistence.ShardingProvider;

import java.util.Map;

/**
 * Created by yinghu on 7/5/2020.
 */
public class MysqlShardingProvider implements ShardingProvider {

    private static JDKLogger log = JDKLogger.getLogger(MysqlShardingProvider.class);


    private String name;
    private int scope;
    private int shards;
    private Shard[] shardList;
    @Override
    public void start() throws Exception {
        //create connections
    }

    @Override
    public void shutdown() throws Exception {
        //close connections
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public int scope() {
        return scope;
    }

    @Override
    public void configure(Map<String, String> properties) {
        this.name = properties.get("name");
        this.scope = Integer.parseInt(properties.get("scope"));
        this.shards = Integer.parseInt(properties.get("shards"));
        this.shardList = new Shard[shards];
    }

    @Override
    public void addShard(Shard shard) {
        shardList[shard.shardNumber]=shard;
    }
}
