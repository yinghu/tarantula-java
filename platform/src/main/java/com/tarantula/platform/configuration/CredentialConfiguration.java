package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class CredentialConfiguration extends Application {

    protected String typeId;
    //private String name;
    public CredentialConfiguration(String typeId,String name,ConfigurableObject configurableObject){
        super(configurableObject);
        this.name = name;
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }

    public boolean setup(DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        return true;
    }

    protected ConfigurationObject saveConfigurationObject(String label,DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        String fileName = header.get(label).getAsString();
        Content conf = deploymentServiceProvider.resource(fileName);
        ConfigurationObject configurationObject = new ConfigurationObject(label);
        configurationObject.distributionKey(this.distributionKey());
        if(conf.existed()){
            if(dataStore.load(configurationObject)){
                configurationObject.value(conf.data());
                dataStore.update(configurationObject);
            }
            else{
                configurationObject.value(conf.data());
                dataStore.createIfAbsent(configurationObject,false);
            }
            deploymentServiceProvider.deleteResource(fileName);
        }
        else{
            dataStore.load(configurationObject);
        }
        IndexSet index = new IndexSet("keys");
        index.distributionKey(this.distributionKey());
        dataStore.createIfAbsent(index,true);
        if(index.addKey(configurationObject.key().asString())){
            dataStore.update(index);
        }
        return configurationObject;
    }
}
