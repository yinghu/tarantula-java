package com.tarantula.platform.service.persistence;

import com.tarantula.Recoverable;
import com.tarantula.platform.util.ShardSetup;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.util.Map;
/**
 * Created by yinghu on 7/5/2020.
 */
public class Shard {
    final public int shardNumber;
    private BasicDataSource dataSource = new BasicDataSource();

    public Shard(int shardNumber){
        this.shardNumber = shardNumber;
    }
    public void configuration(Map<String,String> config) throws Exception{
        ShardSetup.createShard(config.get("database"));
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(config.get("url")+ Recoverable.PATH_SEPARATOR+config.get("database"));
        dataSource.setUsername(config.get("user"));
        dataSource.setPassword(config.get("password"));
        // Connection pooling properties
        int poolSize = Integer.parseInt(config.get("poolSize"));
        dataSource.setInitialSize(poolSize);
        dataSource.setMaxIdle(poolSize);
        dataSource.setMaxTotal(poolSize);
        dataSource.setMinIdle(poolSize);
    }
    public Connection connection() throws Exception{
        return dataSource.getConnection();
    }
    public void shutdown() throws Exception{
        this.dataSource.close();
    }
}
