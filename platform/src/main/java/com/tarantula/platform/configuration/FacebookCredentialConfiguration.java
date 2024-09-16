package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class FacebookCredentialConfiguration extends CredentialConfiguration {

    private FacebookLogin facebookLogin;

    public FacebookCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
        this.name = OnAccess.FACEBOOK;
    }

    public FacebookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.FACEBOOK,configurableObject);
    }

    public FacebookLogin facebookLogin(){
        return facebookLogin;
    }

    public boolean setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        Content content = super.content("Login");
        if(!content.existed()) return false;
        facebookLogin = new FacebookLogin(JsonUtil.parse(content.data()));
        return facebookLogin.validate(serviceContext);
    }

}
