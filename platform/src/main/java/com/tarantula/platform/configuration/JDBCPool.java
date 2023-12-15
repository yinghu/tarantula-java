package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ServiceContext;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;

public class JDBCPool implements VendorValidator{

    private BasicDataSource dataSource;

    public JDBCPool(JsonObject props){
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(props.get("DriverClassName").getAsString());
        dataSource.setUrl(props.get("Url").getAsString()+props.get("Database").getAsString());
        dataSource.setUsername(props.get("User").getAsString());
        dataSource.setPassword(props.get("Password").getAsString());
        dataSource.setMaxTotal(props.get("PoolSize").getAsInt());

    }
    public boolean validate(ServiceContext serviceContext){
        try(Connection conn = dataSource.getConnection()){
            if(conn.isClosed()) throw new RuntimeException("Database id closed");
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public Connection connection() throws Exception{
        return dataSource.getConnection();
    }
}
