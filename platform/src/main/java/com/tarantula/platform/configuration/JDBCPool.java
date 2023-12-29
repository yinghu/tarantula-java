package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class JDBCPool implements VendorValidator{

    private static final TarantulaLogger logger = JDKLogger.getLogger(JDKLogger.class);
    private BasicDataSource dataSource;
    private JsonObject props;
    private String node;
    public JDBCPool(JsonObject props){
        this.props = props;
    }
    public boolean validate(ServiceContext serviceContext){
        this.node = serviceContext.node().nodeName();
        String url = props.get("Url").getAsString();
        String db = props.get("Database").getAsString();
        Properties properties = new Properties();
        properties.setProperty("user",props.get("User").getAsString());
        properties.setProperty("password",props.get("Password").getAsString());
        try(Connection conn = DriverManager.getConnection(url,properties); Statement cmd = conn.createStatement()){
            cmd.execute("CREATE DATABASE "+db);
            return true;
        }
        catch (Exception ex){
            //Ignore if database already exists
            logger.warn("Should be here if database exists on server : "+db,ex);
            return true;
        }
        finally {
            try(Connection conn = DriverManager.getConnection(url+db,properties); Statement cmd = conn.createStatement()){
                for(String metrics : serviceContext.metricsList()){
                    cmd.execute("CREATE TABLE IF NOT EXISTS "+metrics.replaceAll("-","_")+" (category varchar(50) NOT NULL,total DOUBLE PRECISION NOT NULL, node char(3) NOT NULL , updated TIMESTAMP NOT NULL, PRIMARY KEY(category,node))");
                }
            }
            catch (Exception ex){
                logger.warn("should not be here",ex);
                throw new RuntimeException(ex);
            }
            createPool();
        }
    }

    public Connection connection() throws Exception{
        return dataSource.getConnection();
    }
    public String node(){
        return node;
    }

    private void createPool(){
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(props.get("DriverClassName").getAsString());
        dataSource.setUrl(props.get("Url").getAsString()+props.get("Database").getAsString());
        dataSource.setUsername(props.get("User").getAsString());
        dataSource.setPassword(props.get("Password").getAsString());
        dataSource.setMaxTotal(props.get("PoolSize").getAsInt());
    }
}
