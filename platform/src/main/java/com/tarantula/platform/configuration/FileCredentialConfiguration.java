package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.item.ConfigurableObject;


public class FileCredentialConfiguration extends CredentialConfiguration {

    private ServiceContext serviceContext;
    public FileCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,configurableObject.configurationName(),configurableObject);
        this.typeId = typeId;
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        this.serviceContext = serviceContext;
        Content content = serviceContext.deploymentServiceProvider().resource(header.get("File").getAsString());
        return content.existed();
    }

    public byte[] load(){
        Content content = serviceContext.deploymentServiceProvider().resource(header.get("File").getAsString());
        return content.existed()?content.data():new byte[0];
    }

}
