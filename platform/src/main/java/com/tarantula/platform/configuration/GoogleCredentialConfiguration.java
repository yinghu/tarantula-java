package com.tarantula.platform.configuration;


import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class GoogleCredentialConfiguration extends Application {

    private String typeId;
    public GoogleCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }
    public String typeId(){
        return typeId;
    }
    public String name(){
        return OnAccess.GOOGLE;
    }
    public String webClient(){
        return header.get("WebClient").getAsString();
    }

    public String serviceAccount(){
        return header.get("ServiceAccount").getAsString();
    }

    public String description(){
        return header.get("Description").getAsString();
    }




}
