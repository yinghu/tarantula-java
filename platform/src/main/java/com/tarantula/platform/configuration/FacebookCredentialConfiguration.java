package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.ConfigurableObject;

public class FacebookCredentialConfiguration extends CredentialConfiguration {


    public FacebookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.FACEBOOK,configurableObject);
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
