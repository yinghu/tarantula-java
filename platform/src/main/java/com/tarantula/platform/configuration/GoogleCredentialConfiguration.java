package com.tarantula.platform.configuration;


import com.google.gson.JsonObject;
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



    public GoogleWebClient webClient(){
        return webClient;
    }
    public GoogleServiceAccount serviceAccount(){
        return serviceAccount;
    }

    @Override
    public boolean setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Content webData = super.content("WebClient");
        if(!webData.existed()) return false;
        Content serviceData = super.content("ServiceAccount");
        if(!serviceData.existed()) return false;
        webClient = new GoogleWebClient(JsonUtil.parse(webData.data()));
        serviceAccount = new GoogleServiceAccount(JsonUtil.parse(serviceData.data()));
        return webClient().validate(serviceContext) && serviceAccount().validate(serviceContext);
    }
}
