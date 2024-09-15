package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class AppleCredentialConfiguration extends CredentialConfiguration {

    private AppleStoreKey appleStoreKey;

    public AppleCredentialConfiguration(String typeId, JsonObject payload){
        super(typeId,payload);
        this.name = OnAccess.APPLE;
    }

    public AppleCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.APPLE,configurableObject);
    }

    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("StoreKey",serviceContext.deploymentServiceProvider(),dataStore);
        appleStoreKey = new AppleStoreKey(JsonUtil.parse(configurationObject.value()));
        return appleStoreKey.validate(serviceContext);
    }

    public AppleStoreKey appleStoreKey(){
        return appleStoreKey;
    }

    public boolean setup(ServiceContext serviceContext){
        Content content = serviceContext.node().homingAgent().onDownload(header.get("StoreKey").getAsString());
        if(!content.existed()) return false;
        appleStoreKey = new AppleStoreKey(JsonUtil.parse(content.data()));
        return appleStoreKey.validate(serviceContext);
    }
}
