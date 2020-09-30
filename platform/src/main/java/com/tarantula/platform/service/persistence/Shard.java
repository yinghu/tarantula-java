package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.util.ShardSetup;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.util.Map;
/**
 * Created by yinghu on 7/5/2020.
 */
public class Shard implements Serviceable {
    final public int shardNumber;
    final private boolean enabled;

    private BasicDataSource dataSource = new BasicDataSource();

    public Shard(int shardNumber,boolean  enabled){
        this.shardNumber = shardNumber;
        this.enabled = enabled;
    }
    public void configuration(Map<String,String> config) throws Exception{
        if(!enabled){
            return;
        }
        ShardSetup.createShard(config.get("database"),config);
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

    @Override
    public void start() throws Exception {

    }
    @Override
    public void shutdown() throws Exception{
        if(enabled){
            this.dataSource.close();
        }
    }
}
