package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class GooglePlayConfiguration extends Application {


    private String typeId;

    public GooglePlayConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }

    public String name(){
        return OnAccess.GOOGLE;
    }
    //android
    public String applicationId(){
        return header.get("ApplicationId").getAsString();
    }
    public String accessKey(){
        return header.get("AccessKey").getAsString();
    }

    //web
    public String clientId(){
        return header.get("WebClientId").getAsString();
    }
    public String projectId(){
        return header.get("WebSecretKey").getAsString();
    }
    public String clientSecret(){
        return header.get("WebClientSecret").getAsString();
    }

}
