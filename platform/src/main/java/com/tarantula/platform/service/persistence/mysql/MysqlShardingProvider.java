package com.tarantula.platform.service.persistence.mysql;

import com.tarantula.Distributable;
import com.tarantula.Metadata;
import com.tarantula.Recoverable;
import com.tarantula.RecoverableRegistry;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.ServiceContext;
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
    private ServiceContext serviceContext;
    private boolean enabled;

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
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }


    @Override
    public int scope() {
        return scope;
    }

    @Override
    public void configure(Map<String, String> properties) {
        this.name = properties.get("name");
        //this.node = properties.get("node");
        this.scope = Integer.parseInt(properties.get("scope"));
        this.shards = Integer.parseInt(properties.get("shards"));
        this.enabled = Boolean.parseBoolean(properties.get("enabled"));
        int pno = scope== Distributable.INTEGRATION_SCOPE?Integer.parseInt(properties.get("p1")):Integer.parseInt(properties.get("p2"));
        this.shardList = new Shard[shards];
        log.warn("Sharding provider partitions->"+pno+"<>"+scope+"<>"+name);
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
            log.warn("registering data store->"+name);
            for(Shard shard : shardList){
                Connection con = shard.connection();
                Statement cmd = con.createStatement();
                cmd.execute("CREATE TABLE IF NOT EXISTS "+name+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON,c INT NOT NULL,f INT NOT NULL, INDEX ix_c(c), INDEX ix_f(f))");
                cmd.close();
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
                for(int i=0;i<partitions;i++){
                    try{
                        cmd.execute("CREATE TABLE IF NOT EXISTS "+prefix+"_"+i+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON,c INT NOT NULL,f INT NOT NULL, INDEX ix_c(c),INDEX ix_f(f))");
                    }catch (Exception ignore){
                        log.warn("Error on register data store"+prefix+i+"->"+ignore.getMessage());
                    }
                }
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
        try{
            Map<String,Object> data = t.toMap();
            Connection connection = shardList[metadata.partition()%shards].connection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+metadata.source()+" VALUES(?,?,?,?)");
                preparedStatement.setString(1,key);
                String ret = SystemUtil.toJsonString(data);
                log.warn("CREATE KEY->"+key+"<><>"+ret+"<><>"+metadata.source());
                preparedStatement.setString(2, ret);
                preparedStatement.setInt(3,t.getClassId());
                preparedStatement.setInt(4,t.getFactoryId());
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
    @Override
    public <T extends Recoverable> T load(Metadata metadata,String key){
        if(!enabled){
            log.warn("Data backup is disabled->"+key);
            return null;
        }
        try{
            Connection connection = shardList[metadata.partition()%shards].connection();
            try{
                log.warn("LOAD KEY->"+key+"<><>"+metadata.source());
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT v,c,f FROM "+metadata.source()+" WHERE k=?");
                preparedStatement.setString(1,key);
                ResultSet rs = preparedStatement.executeQuery();
                T ret =null;
                if(rs.next()){
                    byte[] _ret = rs.getString("v").getBytes();
                    int c = rs.getInt("c");
                    int f = rs.getInt("f");
                    RecoverableRegistry rgis = serviceContext.recoverableRegistry(f);
                    ret = (T)rgis.create(c);
                    ret.fromMap(SystemUtil.toMap(_ret));
                }
                rs.close();
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
    @Override
    public <T extends Recoverable> byte[] update(Metadata metadata,String key,T t){
        if(!enabled){
            log.warn("Data backup is disabled->"+key);
            return SystemUtil.toJson(t.toMap());
        }
        try{
            Connection connection = shardList[metadata.partition()%shards].connection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE "+metadata.source()+" SET v=? WHERE k=?");
                String ret = SystemUtil.toJsonString(t.toMap());
                log.warn("UPDATE KEY->"+key+"<><>"+ret+"<><>"+metadata.source());
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
    /**
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
    }**/
    /**
    @Override
    public void onBucket(int bucket, int state) {
        if(state==BucketReceiver.CLOSE){//close always
            partitionStates[bucket].opening = false;
            //log.warn(node+" is closing bucket->"+bucket+" with version->"+partitionStates[bucket].version);
            return;
        }
        else if(state==BucketReceiver.OPEN&&partitionStates[bucket].opening){//keep open
            //log.warn(node+" is keeping up bucket->"+bucket+" with version->"+partitionStates[bucket].version);
            return;
        }
        partitionStates[bucket].opening = true;
        try{
            Connection connection = shardList[bucket%shards].connection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE meta_info SET n=?,v=v+1 WHERE p=?");
                preparedStatement.setString(1,node);
                preparedStatement.setInt(2,bucket);
                preparedStatement.execute();
                preparedStatement.close();
                preparedStatement = connection.prepareStatement("SELECT v FROM meta_info WHERE p=?");
                preparedStatement.setInt(1,bucket);
                ResultSet rs = preparedStatement.executeQuery();
                if(rs.next()){
                    partitionStates[bucket].version = rs.getInt("v");
                }
                rs.close();
                preparedStatement.close();
                log.warn(node+" is taking over bucket->"+bucket+" with version->"+partitionStates[bucket].version);
            }catch (Exception eex){
                throw new RuntimeException(eex.getMessage());
            }
            finally {
                connection.close();
            }
        }catch (Exception ex){
            log.warn("error on update->"+ex.getMessage());
        }
    }**/
}
