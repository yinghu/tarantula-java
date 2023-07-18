package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class FacebookCredentialConfiguration extends CredentialConfiguration {

    private FacebookLogin facebookLogin;
    public FacebookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.FACEBOOK,configurableObject);
    }
    public boolean setup(DeploymentServiceProvider deploymentServiceProvider, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("Login",deploymentServiceProvider,dataStore);
        facebookLogin = new FacebookLogin(JsonUtil.parse(configurationObject.value()));
        return true;
    }

    public FacebookLogin facebookLogin(){
        return facebookLogin;
    }
}
