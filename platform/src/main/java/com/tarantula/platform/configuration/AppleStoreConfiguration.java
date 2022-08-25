package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class AppleStoreConfiguration extends Application {

    private String typeId;
    public AppleStoreConfiguration(String typeId,ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }
    public String typeId(){
        return typeId;
    }
    public String name(){
        return OnAccess.APPLE_STORE;
    }
    public String verifyUrl(){
        return header.get("VerifyUrl").getAsString();
    }
    public String secureKey(){
        return header.get("SecureKey").getAsString();
    }
}
