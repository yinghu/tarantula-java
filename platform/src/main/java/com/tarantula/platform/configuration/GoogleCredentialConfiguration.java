package com.tarantula.platform.configuration;


import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class GoogleCredentialConfiguration extends Application {

    private String typeId;
    private GoogleWebClient webClient;
    private GoogleServiceAccount serviceAccount;
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

    public String description(){
        return header.get("Description").getAsString();
    }

    public void setup(DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        ConfigurationObject web = _setup("WebClient",deploymentServiceProvider,dataStore);
        ConfigurationObject service = _setup("ServiceAccount",deploymentServiceProvider,dataStore);
        webClient = new GoogleWebClient(JsonUtil.parse(web.value()));
        serviceAccount = new GoogleServiceAccount(JsonUtil.parse(service.value()));
    }

    public GoogleWebClient webClient(){
        return webClient;
    }
    public GoogleServiceAccount serviceAccount(){
        return serviceAccount;
    }
    private ConfigurationObject _setup(String label,DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
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
        return configurationObject;
    }

}
