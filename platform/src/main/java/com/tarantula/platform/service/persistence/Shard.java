package com.tarantula.platform.service.persistence;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by yinghu on 7/5/2020.
 */
public class Shard {
    final public int shardNumber;
    private Map<String,String> properties;
    private BasicDataSource dataSource = new BasicDataSource();

    public Shard(int shardNumber){
        this.shardNumber = shardNumber;
        properties = new HashMap<>();
    }
    public void configuration(Map<String,String> config) throws Exception{
        properties.putAll(config);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(config.get("url"));
        dataSource.setUsername(config.get("user"));
        dataSource.setPassword(config.get("password"));
        // Connection pooling properties
        dataSource.setInitialSize(3);
        dataSource.setMaxIdle(5);
        dataSource.setMaxTotal(5);
        dataSource.setMinIdle(0);
    }
    public Connection connection() throws Exception{
        return dataSource.getConnection();
    }
    public void shutdown() throws Exception{
        this.dataSource.close();
    }
}
