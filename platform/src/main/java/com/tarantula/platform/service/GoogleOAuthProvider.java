package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public class GoogleOAuthProvider extends AuthObject {

    private Map<String,GoogleOAuthTokenValidator> googleIdTokenVerifiers;
    public GoogleOAuthProvider(Map<String,GoogleOAuthTokenValidator> validatorMappings){
        super(OnAccess.GOOGLE,"","","","","",new String[0]);
        this.googleIdTokenVerifiers = validatorMappings;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        googleIdTokenVerifiers.forEach((k,v)->{
            v.registerMetricsLister(this.metricsListener);
            v.setup(serviceContext);
        });
    }

    @Override
    public boolean validate(Map<String,Object> params){
        try{
            GoogleOAuthTokenValidator googleIdTokenVerifier = googleIdTokenVerifiers.get(params.get("typeId"));
            return googleIdTokenVerifier.validate(params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public String clientId(String typeId){
        GoogleOAuthTokenValidator googleOAuthTokenValidator = googleIdTokenVerifiers.get(typeId);
        return googleOAuthTokenValidator!=null?googleOAuthTokenValidator.clientId():"";
    }

}
