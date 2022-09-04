package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class FacebookConfiguration extends Application {

    private String typeId;

    public FacebookConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }

    public String name(){
        return OnAccess.FACEBOOK;
    }

    public String appName(){
        return header.get("AppName").getAsString();
    }
    public String appId(){
        return header.get("AppId").getAsString();
    }
    public String secretKey(){
        return header.get("SecretKey").getAsString();
    }
}
