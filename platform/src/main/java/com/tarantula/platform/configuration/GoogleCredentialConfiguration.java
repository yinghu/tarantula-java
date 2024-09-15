package com.tarantula.platform.configuration;


import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;

import com.tarantula.platform.item.ConfigurableObject;

public class GoogleCredentialConfiguration extends CredentialConfiguration {

    private GoogleWebClient webClient;
    private GoogleServiceAccount serviceAccount;

    public GoogleCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
        this.name = OnAccess.GOOGLE;
    }

    public GoogleCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.GOOGLE,configurableObject);
    }

    public String packageName(){
        return header.get("PackageName").getAsString();
    }

    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject web = saveConfigurationObject("WebClient",serviceContext.deploymentServiceProvider(),dataStore);
        ConfigurationObject service = saveConfigurationObject("ServiceAccount",serviceContext.deploymentServiceProvider(),dataStore);
        webClient = new GoogleWebClient(JsonUtil.parse(web.value()));
        serviceAccount = new GoogleServiceAccount(JsonUtil.parse(service.value()));
        return webClient().validate(serviceContext) && serviceAccount().validate(serviceContext);
    }

    public GoogleWebClient webClient(){
        return webClient;
    }
    public GoogleServiceAccount serviceAccount(){
        return serviceAccount;
    }

    @Override
    public boolean setup(ServiceContext serviceContext) {
        Content webData = serviceContext.node().homingAgent().onDownload(header.get("WebClient").getAsString());
        if(!webData.existed()) return false;
        Content serviceData = serviceContext.node().homingAgent().onDownload(header.get("ServiceAccount").getAsString());
        if(!serviceData.existed()) return false;
        webClient = new GoogleWebClient(JsonUtil.parse(webData.data()));
        serviceAccount = new GoogleServiceAccount(JsonUtil.parse(serviceData.data()));
        return webClient().validate(serviceContext) && serviceAccount().validate(serviceContext);
    }
}
