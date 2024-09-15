package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;

public class CredentialConfiguration extends Application{

    protected String typeId;

    public CredentialConfiguration(String typeId, JsonObject payload){
        this.typeId = typeId;
        this.application = payload;
        this.configurationName = payload.get("ConfigurationName").getAsString();
        this.name = this.configurationName;
    }

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

    protected ConfigurationObject saveConfigurationObject(String nLabel,DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        String fileName = header.get(nLabel).getAsString();
        Content conf = deploymentServiceProvider.resource(fileName);
        RecoverableQuery<ConfigurationObject> query = new RecoverableQuery<>(this.key(),ConfigurationObject.LABEL, ItemPortableRegistry.CONFIGURATION_OBJECT_CID,ItemPortableRegistry.INS);
        ConfigurationObject[] pending = {null};
        dataStore.list(query,(t)->{
            if(t.name().equals(nLabel)){
                pending[0]=t;
                return false;
            }
            return true;
        });
        if(!conf.existed() && pending[0]==null) throw new IllegalArgumentException("config content not existed ["+nLabel+"]");
        if(conf.existed() && pending[0]==null){
            pending[0] = new ConfigurationObject(nLabel);
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

    public void release(){

    }

    public boolean setup(ServiceContext serviceContext){
        return false;
    }
    public int publishId(){
        return application.get("ConfigurationPublishId").getAsInt();
    }
}
