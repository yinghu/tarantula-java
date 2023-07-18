package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class FacebookCredentialConfiguration extends CredentialConfiguration {

    private FacebookLogin facebookLogin;
    public FacebookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.FACEBOOK,configurableObject);
    }
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("Login",serviceContext.deploymentServiceProvider(),dataStore);
        facebookLogin = new FacebookLogin(JsonUtil.parse(configurationObject.value()));
        return facebookLogin.validate(serviceContext);
    }

    public FacebookLogin facebookLogin(){
        return facebookLogin;
    }
}
