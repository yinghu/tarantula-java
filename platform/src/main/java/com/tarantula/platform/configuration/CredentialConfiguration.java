package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;
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
        RecoverableQuery<ConfigurationObject> query = new RecoverableQuery<>(new SnowflakeKey(this.distributionId),ConfigurationObject.LABEL, ItemPortableRegistry.CONFIGURATION_OBJECT_CID,ItemPortableRegistry.INS);
        ConfigurationObject[] pending = {null};
        dataStore.list(query,(t)->{
            if(t.name().equals(label)){
                pending[0]=t;
                return false;
            }
            return true;
        });
        if(!conf.existed() && pending[0]==null) throw new IllegalArgumentException("config content not existed ["+label+"]");
        if(conf.existed() && pending[0]==null){
            pending[0] = new ConfigurationObject(label);
            pending[0].value(conf.data());
            pending[0].ownerKey(this.key());
            dataStore.create(pending[0]);
            deploymentServiceProvider.deleteResource(fileName);
            return pending[0];
        }
        if(conf.existed() && pending[0]!=null) {
            pending[0].value(conf.data());
            dataStore.update(pending[0]);
            deploymentServiceProvider.deleteResource(fileName);
            return pending[0];
        }
        return pending[0];
    }
}
