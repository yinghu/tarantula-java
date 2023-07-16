package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.ConfigurableObject;

public class AppleCredentialConfiguration extends CredentialConfiguration {

    public AppleCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.APPLE_STORE,configurableObject);

    }

    public boolean isSandbox(){
        return header.get("IsSandbox").getAsBoolean();
    }
    public String secureKey(){
        return header.get("SecretKey").getAsString();
    }
}
