package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.Metadata;
import com.tarantula.Recoverable;
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
    private boolean enabled;
    private boolean backup;
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
        this.enabled = Boolean.parseBoolean(properties.get("enabled"));
        this.backup = Boolean.parseBoolean(properties.get("backup"));
        this.shardList = new Shard[shards];
    }

    @Override
    public void addShard(Shard shard) {
        shardList[shard.shardNumber]=shard;
    }
    @Override
    public void registerDataStore(String name){
        if(!enabled){
            log.warn("Data backup is disabled->"+name);
            return;
        }
        try{
            for(Shard shard : shardList){
                Connection con = shard.connection();
                Statement cmd = con.createStatement();
                cmd.execute("CREATE TABLE IF NOT EXISTS "+name+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON,c INT NOT NULL,f INT NOT NULL, INDEX ix_c(c), INDEX ix_f(f))");
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
        if(!enabled){
            log.warn("Data backup is disabled->"+prefix);
            return;
        }
        try{
            log.warn("registering data store->"+prefix+"<>"+partitions);
            for(Shard shard : shardList){
                Connection con = shard.connection();
                Statement cmd = con.createStatement();
                PreparedStatement pstm = con.prepareStatement("INSERT INTO meta_info VALUES (?,?,?,?,?)");
                for(int i=0;i<partitions;i++){
                    try{
                        cmd.execute("CREATE TABLE IF NOT EXISTS "+prefix+"_"+i+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON,c INT NOT NULL,f INT NOT NULL, INDEX ix_c(c),INDEX ix_f(f))");
                        pstm.setString(1,prefix+"_"+i);
                        pstm.setString(2,"node");
                        pstm.setString(3,"bucket");
                        pstm.setInt(4,0);
                        pstm.setInt(5,0);
                        pstm.execute();
                        pstm.clearParameters();
                    }catch (Exception ignore){
                        log.warn("Error on register data store"+prefix+i+"->"+ignore.getMessage());
                    }
                }
                pstm.close();
                cmd.close();
                con.close();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public <T extends Recoverable> byte[] create(Metadata metadata, String key, T t){
        if(!enabled){
            log.warn("Data backup is disabled->"+key);
            return SystemUtil.toJson(t.toMap());
        }
        return null;
    }
    public byte[] create(Metadata metadata, String key, Map<String,Object> data){
        if(!enabled){
            log.warn("Data backup is disabled->"+key);
            return SystemUtil.toJson(data);
        }
        try{
            Connection connection = shardList[metadata.partition()%shards].connection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+metadata.source()+" VALUES(?,?,?,?)");
                preparedStatement.setString(1,key);
                String ret = SystemUtil.toJsonString(data);
                log.warn("CREATE KEY->"+key+"<><>"+ret);
                preparedStatement.setString(2, ret);
                preparedStatement.setInt(3,metadata.classId());
                preparedStatement.setInt(4,metadata.factoryId());
                preparedStatement.execute();
                preparedStatement.close();
                return ret.getBytes();
            }catch (Exception eex){
                throw new RuntimeException(eex.getMessage());
            }
            finally {
                connection.close();
            }
        }catch (Exception ex){
            log.warn("error on create->"+ex.getMessage());
            return null;
        }
    }
    public byte[] load(Metadata metadata,String key){
        if(!enabled){
            log.warn("Data backup is disabled->"+key);
            return null;
        }
        try{
            Connection connection = shardList[metadata.partition()%shards].connection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT v FROM "+metadata.source()+" WHERE k=?");
                preparedStatement.setString(1,key);
                ResultSet rs = preparedStatement.executeQuery();
                byte[] ret = null;
                if(rs.next()){
                    ret = rs.getString("v").getBytes();
                }
                preparedStatement.close();
                return ret;
            }catch (Exception eex){
                throw new RuntimeException(eex.getMessage());
            }
            finally {
                connection.close();
            }
        }catch (Exception ex){
            log.warn("error on load->"+ex.getMessage());
            return null;
        }
    }
    public byte[] update(Metadata metadata,String key,Map<String,Object> data){
        if(!enabled){
            log.warn("Data backup is disabled->"+key);
            return SystemUtil.toJson(data);
        }
        try{
            Connection connection = shardList[metadata.partition()%shards].connection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE "+metadata.source()+" SET v=? WHERE k=?");
                String ret = SystemUtil.toJsonString(data);
                log.warn("UPDATE KEY->"+key+"<><>"+ret);
                preparedStatement.setString(1,ret);
                preparedStatement.setString(2,key);
                preparedStatement.execute();
                preparedStatement.close();
                return ret.getBytes();
            }catch (Exception eex){
                throw new RuntimeException(eex.getMessage());
            }
            finally {
                connection.close();
            }
        }catch (Exception ex){
            log.warn("error on update->"+ex.getMessage());
            return null;
        }
    }
}
