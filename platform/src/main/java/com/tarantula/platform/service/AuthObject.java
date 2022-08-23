package com.tarantula.platform.service;


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
    protected String typeId;
    protected final String name;
    protected   String clientId;
    protected   String secureKey;
    protected   String authUri;
    protected   String tokenUri;
    protected   String certUri;
    protected   String[] origins;
    protected ServiceContext serviceContext;
    protected MetricsListener metricsListener;
    public AuthObject(String name,String clientId,String secureKey,String authUri,String tokenUri,String certUri,String[] origins){
        this.name = name;
        this.clientId = clientId;
        this.secureKey = secureKey;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
        this.certUri = certUri;
        this.origins = origins;
        this.metricsListener = (k,v)->{};
    }

    @Override
    public String name(){
        return this.name;
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
}
