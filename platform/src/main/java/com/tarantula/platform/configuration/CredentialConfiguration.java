package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class CredentialConfiguration extends Application {

    protected String typeId;
    private String name;
    public CredentialConfiguration(String typeId,String name,ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }
    public String name(){
        return name;
    }
    public boolean setup(DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        return true;
    }
}
