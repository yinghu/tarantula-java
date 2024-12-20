package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class AppleCredentialConfiguration extends CredentialConfiguration {

    private TarantulaLogger logger = JDKLogger.getLogger(AppleCredentialConfiguration.class);

    private AppleStoreKey appleStoreKey;


    public AppleCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.APPLE,configurableObject);
    }

    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        Content content = serviceContext.deploymentServiceProvider().resource(header.get("StoreKey").getAsString());
        if(!content.existed()){
            logger.warn("No apple store key loaded : "+header.get("StoreKey").getAsString());
            return false;
        }
        appleStoreKey = new AppleStoreKey(this.header,content);
        return appleStoreKey.validate(serviceContext);
    }

    public AppleStoreKey appleStoreKey(){
        return appleStoreKey;
    }

    public boolean setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        Content content = super.content("StoreKey");
        if(!content.existed()) return false;
        appleStoreKey = new AppleStoreKey(this.header,content);
        return appleStoreKey.validate(serviceContext);
    }
}
