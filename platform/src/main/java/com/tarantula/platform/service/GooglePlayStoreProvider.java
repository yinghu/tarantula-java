package com.tarantula.platform.service;

import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public class GooglePlayStoreProvider extends AuthObject{

    private Map<String,GoogleStorePurchaseValidator> googleStorePurchaseValidators;

    public GooglePlayStoreProvider(Map<String,GoogleStorePurchaseValidator> validatorMappings){
        super("googleStore","","","","","",new String[0]);
        this.googleStorePurchaseValidators  = validatorMappings;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        googleStorePurchaseValidators.forEach((k,v)->{
            v.registerMetricsLister(this.metricsListener);
            v.setup(serviceContext);
        });
    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            GoogleStorePurchaseValidator googleStorePurchaseValidator = googleStorePurchaseValidators.get(params.get("typeId"));
            return googleStorePurchaseValidator.validate(params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

}
