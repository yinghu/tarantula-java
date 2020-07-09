package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.Metadata;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.persistence.Shard;
import com.tarantula.platform.service.persistence.ShardingProvider;
import com.tarantula.platform.util.SystemUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                cmd.execute("CREATE TABLE IF NOT EXISTS "+name+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON)");
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
                }catch (Exception ignore){
                    log.warn("Error on register data store"+name+"->"+ignore.getMessage());
                }
                con.close();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public  void registerDataStore(String prefix,int partitions){
        try{
            for(Shard shard : shardList){
                Connection con = shard.connection();
                Statement cmd = con.createStatement();
                PreparedStatement pstm = con.prepareStatement("INSERT INTO meta_info VALUES (?,?,?,?,?)");
                for(int i=0;i<partitions;i++){
                    cmd.addBatch("CREATE TABLE IF NOT EXISTS "+prefix+"_"+i+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON)");
                    try{
                        pstm.setString(1,prefix+"_"+i);
                        pstm.setString(2,"node");
                        pstm.setString(3,"bucket");
                        pstm.setInt(4,0);
                        pstm.setInt(5,0);
                        pstm.execute();
                    }catch (Exception ignore){
                        log.warn("Error on register data store"+prefix+i+"->"+ignore.getMessage());
                    }
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
    public byte[] create(Metadata metadata, String key, Map<String,Object> data){
        try{
            log.warn("DATA TABLE 1->"+metadata.source());
            Connection connection = shardList[metadata.partition()%shards].connection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+metadata.source()+" VALUES(?,?)");
            preparedStatement.setString(1,key);
            String ret = SystemUtil.toJsonString(data);
            preparedStatement.setString(2, ret);
            preparedStatement.execute();
            preparedStatement.close();
            connection.close();
            return ret.getBytes();
        }catch (Exception ex){
            return null;
        }
    }
    public byte[] load(Metadata metadata,String key){
        try{
            log.warn("DATA TABLE 2->"+metadata.source());
            Connection connection = shardList[metadata.partition()%shards].connection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT v FROM "+metadata.source()+" WHERE k=?");
            preparedStatement.setString(1,key);
            ResultSet rs = preparedStatement.executeQuery();
            byte[] ret = null;
            if(rs.next()){
                ret = rs.getString("v").getBytes();
            }
            preparedStatement.close();
            connection.close();
            return ret;
        }catch (Exception ex){
            return null;
        }
    }
    public byte[] update(Metadata metadata,String key,Map<String,Object> data){
        try{
            log.warn("DATA TABLE 3->"+metadata.source());
            Connection connection = shardList[metadata.partition()%shards].connection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE "+metadata.source()+" SET v=? WHERE k=?");
            String ret = SystemUtil.toJsonString(data);
            preparedStatement.setString(1,ret);
            preparedStatement.setString(2,key);
            preparedStatement.execute();
            preparedStatement.close();
            connection.close();
            return ret.getBytes();
        }catch (Exception ex){
            return null;
        }
    }
}
