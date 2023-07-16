package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;

import com.tarantula.platform.item.ConfigurableObject;

public class MySQLCredentialConfiguration extends CredentialConfiguration {



    public MySQLCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId, OnAccess.MYSQL,configurableObject);
        this.typeId = typeId;
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
