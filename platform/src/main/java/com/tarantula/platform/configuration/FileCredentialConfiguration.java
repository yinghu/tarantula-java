package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.item.ConfigurableObject;


public class FileCredentialConfiguration extends CredentialConfiguration {

    private Content content;

    public FileCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
        this.name = this.configurationName;
    }

    public FileCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,configurableObject.configurationName(),configurableObject);
        this.typeId = typeId;
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        content = serviceContext.deploymentServiceProvider().resource(header.get("File").getAsString());
        return content.existed();
    }

    public byte[] load(){
        return content.data();
    }


    @Override
    public boolean setup(ServiceContext serviceContext) {
        String fileName = header.get("File").getAsString();
        content = serviceContext.node().homingAgent().onDownload(fileName);
        return content.existed();
    }
}
