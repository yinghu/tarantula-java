package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.Connection;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;
import org.apache.commons.dbcp2.BasicDataSource;

public class JDBCPoolCredentialConfiguration extends CredentialConfiguration {



    public JDBCPoolCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId, OnAccess.JDBC_SQL,configurableObject);
        this.typeId = typeId;
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("DBCPPool",serviceContext.deploymentServiceProvider(),dataStore);
        JsonObject pool = JsonUtil.parse(configurationObject.value());
        //DriverManager.getDriver()
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(pool.get("DriverClassName").getAsString());
        ds.setUrl(pool.get("Url").getAsString());
        ds.setUsername(pool.get("User").getAsString());
        ds.setPassword(pool.get("Password").getAsString());
        ds.setMaxTotal(pool.get("PoolSize").getAsInt());
        //Connection conn = ds.getConnection();
        System.out.println(pool);
        return true;
        //return endpoint.size()>0;
    }

    public String url(){
        return header.get("Url").getAsString();
    }
    public String database(){
        return header.get("Database").getAsString();
    }
    public String user(){
        return header.get("User").getAsString();
    }
    public String password(){
        return header.get("Password").getAsString();
    }
    public int poolSize(){
        return header.get("PoolSize").getAsInt();
    }
}
