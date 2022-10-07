package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class MySQLConfiguration extends Application {

    private String typeId;

    public MySQLConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }

    public String name(){
        return OnAccess.MYSQL;
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
