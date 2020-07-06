package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.persistence.Shard;
import com.tarantula.platform.service.persistence.ShardingProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
        for(Shard shard : shardList){
            shard.shutdown();
        }
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
    @Override
    public void registerDataStore(String name){
        try{
            for(Shard shard : shardList){
                Connection con = shard.connection();
                Statement cmd = con.createStatement();
                cmd.execute("CREATE TABLE IF NOT EXISTS "+name+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v BLOB)");
                cmd.close();
                try{
                    PreparedStatement pstm = con.prepareStatement("INSERT INTO meta_info VALUES (?,?,?,?,?)");
                    pstm.setString(1,name);
                    pstm.setString(2,"node");
                    pstm.setString(3,"bucket");
                    pstm.setInt(4,0);
                    pstm.setInt(5,0);
                    pstm.execute();
                    pstm.close();
                }catch (Exception ignore){}
                con.close();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void registerDataStore(String prefix,int partitions){
        try{
            for(Shard shard : shardList){
                Connection con = shard.connection();
                Statement cmd = con.createStatement();
                PreparedStatement pstm = con.prepareStatement("INSERT INTO meta_info VALUES (?,?,?,?,?)");
                for(int i=0;i<partitions;i++){
                    cmd.addBatch("CREATE TABLE IF NOT EXISTS "+prefix+"_"+i+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v BLOB)");
                    try{
                        pstm.setString(1,prefix+"_"+i);
                        pstm.setString(2,"node");
                        pstm.setString(3,"bucket");
                        pstm.setInt(4,0);
                        pstm.setInt(5,0);
                        pstm.execute();
                    }catch (Exception ignore){}
                }
                pstm.close();
                cmd.executeBatch();
                cmd.close();
                con.close();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
