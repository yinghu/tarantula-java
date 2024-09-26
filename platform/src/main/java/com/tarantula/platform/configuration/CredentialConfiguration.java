package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;


public class CredentialConfiguration extends Application{

    protected String typeId;
    protected ServiceContext serviceContext;
    //homing agent
    public CredentialConfiguration(String typeId, JsonObject payload){
        super(payload);
        this.typeId = typeId;
    }

    //local
    public CredentialConfiguration(String typeId,String name,ConfigurableObject configurableObject){
        super(configurableObject);
        this.name = name;
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }


    public void release(){

    }

    public boolean setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        return false;
    }
    public int publishId(){
        return header.get("ConfigurationPublishId").getAsInt();
    }
    protected Content content(String contentLabel){
        String fileName = header.get(contentLabel).getAsString();
        if(serviceContext.node().homingAgent().enabled()){
            return serviceContext.node().homingAgent().onDownload(fileName);
        }
        return serviceContext.deploymentServiceProvider().resource(fileName);
    }
}
