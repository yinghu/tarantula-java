package com.tarantula.platform.service.persistence.mysql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableRegistry;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.configuration.MySQLConfiguration;
import com.icodesoftware.util.JsonUtil;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class MysqlBackupProvider implements BackupProvider {

    private static JDKLogger log = JDKLogger.getLogger(MysqlBackupProvider.class);


    private ServiceContext serviceContext;
    private boolean enabled;

    private BasicDataSource dataSource;

    private MySQLConfiguration mySQLConfiguration;

    public MysqlBackupProvider(){

    }

    public MysqlBackupProvider(MySQLConfiguration mySQLConfiguration){
        this.mySQLConfiguration = mySQLConfiguration;
    }

    @Override
    public void start() throws Exception {
        if(!enabled) return;
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(mySQLConfiguration.url()+ Recoverable.PATH_SEPARATOR+mySQLConfiguration.database());
        dataSource.setUsername(mySQLConfiguration.user());
        dataSource.setPassword(mySQLConfiguration.password());
        // Connection pooling properties
        int poolSize = mySQLConfiguration.poolSize();
        dataSource.setInitialSize(poolSize);
        dataSource.setMaxIdle(poolSize);
        dataSource.setMaxTotal(poolSize);
        dataSource.setMinIdle(poolSize);
        dataSource.getConnection();
    }

    public void configure(Map<String,Object> properties){
       if(!enabled) return;
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        String url = ((JsonElement)properties.get("url")).getAsString();
        String db = ((JsonElement)properties.get("database")).getAsString();
        String user = ((JsonElement)properties.get("user")).getAsString();
        String password = ((JsonElement)properties.get("password")).getAsString();
        int poolSize = ((JsonElement)properties.get("poolSize")).getAsInt();
        dataSource.setUrl(url+ Recoverable.PATH_SEPARATOR+db);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        // Connection pooling properties
        dataSource.setInitialSize(poolSize);
        dataSource.setMaxIdle(poolSize);
        dataSource.setMaxTotal(poolSize);
        dataSource.setMinIdle(poolSize);
        try {
            dataSource.getConnection();
        }catch (Exception ex){
            log.error("error on configure",ex);
            this.enabled = false;
        }
    }
    @Override
    public void shutdown() throws Exception {
        if(!enabled) return;
        this.dataSource.close();
    }
    public boolean enabled(){
        return this.enabled;
    }

    @Override
    public void enabled(boolean enabled){
        this.enabled = enabled;
    }
    @Override
    public String name() {
        return this.mySQLConfiguration.typeId();
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    @Override
    public int scope() {
        return Distributable.DATA_SCOPE;
    }


    @Override

    public void registerDataStore(String name){
        if(!enabled){
            return;
        }
        try{
            Connection con = dataSource.getConnection();
            Statement cmd = con.createStatement();
            cmd.execute("CREATE TABLE IF NOT EXISTS "+name+"(k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON,c INT NOT NULL,f INT NOT NULL, INDEX ix_c(c), INDEX ix_f(f))");
            cmd.close();
            con.close();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public  void registerDataStore(String prefix,int partition){
        if(!enabled){
            return;
        }
        try{
            Connection con = dataSource.getConnection();
            Statement cmd = con.createStatement();
            cmd.execute("CREATE TABLE IF NOT EXISTS "+prefix+" (k VARCHAR(100) NOT NULL PRIMARY KEY,v JSON,c INT NOT NULL,f INT NOT NULL, INDEX ix_c(c),INDEX ix_f(f))");
            cmd.close();
            con.close();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> void create(Metadata metadata, String key, T t){
        if(!enabled) return;

        try{
            Map<String,Object> data = t.toMap();
            Connection connection = dataSource.getConnection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+metadata.source()+" VALUES(?,?,?,?)");
                preparedStatement.setString(1,key);
                String ret = JsonUtil.toJsonString(data);
                preparedStatement.setString(2, ret);
                preparedStatement.setInt(3,t.getClassId());
                preparedStatement.setInt(4,t.getFactoryId());
                preparedStatement.execute();
                preparedStatement.close();
                //return ret.getBytes();
            }catch (Exception eex){
                throw new RuntimeException(eex.getMessage());
            }
            finally {
                connection.close();
            }
        }catch (Exception ex){
            log.error("error on create-",ex);
            //return null;
        }
    }

    public <T extends Recoverable> T load(Metadata metadata,String key){
        if(!enabled){
           return null;
        }
        try{
            Connection connection = dataSource.getConnection();
            try{
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
                    ret.fromMap(JsonUtil.toMap(_ret));
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
            log.error("error on load-",ex);
            return null;
        }
    }

    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t){
        if(!enabled) return;
        try{
            Connection connection = dataSource.getConnection();
            try{
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE "+metadata.source()+" SET v=? WHERE k=?");
                String ret = JsonUtil.toJsonString(t.toMap());
                preparedStatement.setString(1,ret);
                preparedStatement.setString(2,key);
                preparedStatement.execute();
                preparedStatement.close();
                //return ret.getBytes();
            }catch (Exception eex){
                throw new RuntimeException(eex.getMessage());
            }
            finally {
                connection.close();
            }
        }catch (Exception ex){
            log.error("error on update->",ex);
        }
    }

}
