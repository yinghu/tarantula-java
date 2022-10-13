package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class WebHookConfiguration extends Application {

    private String typeId;

    public WebHookConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }

    public String name(){
        return OnAccess.WEB_HOOK;
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
