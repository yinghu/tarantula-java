package com.tarantula.platform.service;

import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.Map;

/**
 * Created by yinghu lu on 1/31/2019.
 */
public class AuthObject implements TokenValidatorProvider.AuthVendor {

    private  String name;
    private  String clientId;
    private  String secureKey;
    private  String authUri;
    private  String tokenUri;
    private  String certUri;
    private  String[] origins;
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
    public String clientId() {
        return this.clientId;
    }

    @Override
    public String secureKey() {
        return this.secureKey;
    }

    @Override
    public String authUri() {
        return this.authUri;
    }

    @Override
    public String tokenUri() {
        return this.tokenUri;
    }

    @Override
    public String certUri() {
        return this.certUri;
    }

    @Override
    public String[] origins() {
        return this.origins;
    }
    public void registerMetricsLister(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    @Override
    public boolean validate(Map<String,Object> params){
        return false;
    }
}
