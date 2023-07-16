package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class WebHookCredentialConfiguration extends CredentialConfiguration {



    public WebHookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.WEB_HOOK,configurableObject);

    }

    public String host(){
        return header.get("Host").getAsString();
    }
    public String accessKey(){
        return header.get("AccessKey").getAsString();
    }
    public String path(){
        return header.get("Path").getAsString();
    }

}
