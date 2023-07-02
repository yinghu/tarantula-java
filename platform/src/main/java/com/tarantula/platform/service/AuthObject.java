package com.tarantula.platform.service;


import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.Map;

public class AuthObject implements TokenValidatorProvider.AuthVendor {

    protected static String ACCEPT = "Accept";
    protected static String ACCEPT_JSON = "application/json";
    protected static int TIMEOUT = 10;
    protected static String CONTENT_TYPE = "Content-type";
    protected static String CONTENT_FORM = "application/x-www-form-urlencoded";
    protected static String AUTHORIZATION = "Authorization";
    protected final String typeId;

    protected   String clientId;

    protected ServiceContext serviceContext;
    protected MetricsListener metricsListener;
    protected MetricsListener applicationMetricsListener;
    protected TokenValidatorProvider tokenValidatorProvider;

    protected TarantulaLogger logger;

    public AuthObject(String typeId,String clientId){
        this.typeId = typeId;
        this.clientId = clientId;
        this.metricsListener = (k,v)->{};
    }

    @Override
    public String name(){
        return typeId;
    }

    @Override
    public String typeId(){
        return typeId;
    }

    @Override
    public String clientId() {
        return this.clientId;
    }

    @Override
    public String clientId(String typeId) {
        return this.clientId;
    }


    @Override
    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        tokenValidatorProvider = (TokenValidatorProvider)serviceContext.serviceProvider(TokenValidatorProvider.NAME);
    }
    public void registerMetricsLister(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    @Override
    public boolean validate(Map<String,Object> params){
        return false;
    }

    @Override
    public boolean upload(String s, byte[] bytes) {
        return false;
    }

    protected void onMetrics(String category){
        try{
            if(applicationMetricsListener!=null) applicationMetricsListener.onUpdated(category,1);
            metricsListener.onUpdated(category,1);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
