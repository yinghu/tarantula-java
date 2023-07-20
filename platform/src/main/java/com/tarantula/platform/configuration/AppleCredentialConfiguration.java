package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class AppleCredentialConfiguration extends CredentialConfiguration {

    private AppleStoreKey appleStoreKey;
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
}
