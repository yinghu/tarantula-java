package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.presence.MappingObject;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;

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

    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        return true;
    }

    protected ConfigurationObject saveConfigurationObject(String label,DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        String fileName = header.get(label).getAsString();
        Content conf = deploymentServiceProvider.resource(fileName);
        RecoverableQuery<MappingObject> query = new RecoverableQuery<>(new SnowflakeKey(this.distributionId),ConfigurationObject.LABEL, ItemPortableRegistry.CONFIGURABLE_OBJECT_CID,ItemPortableRegistry.INS);

        ConfigurationObject configurationObject = new ConfigurationObject(label);
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

        return configurationObject;
    }
}
