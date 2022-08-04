package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThirdPartyServiceProvider implements TokenValidatorProvider.AuthVendor {

    private final ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> aMap;
    private final String name;
    private final MetricsListener metricsListener;
    private final ServiceContext serviceContext;

    public ThirdPartyServiceProvider(String name,MetricsListener metricsListener,ServiceContext serviceContext){
        this.name = name;
        this.metricsListener = metricsListener;
        this.serviceContext = serviceContext;
        this.aMap = new ConcurrentHashMap<>();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String clientId() {
        return null;
    }

    @Override
    public String clientId(String s) {
        return null;
    }

    @Override
    public String secureKey() {
        return null;
    }

    @Override
    public String authUri() {
        return null;
    }

    @Override
    public String tokenUri() {
        return null;
    }

    @Override
    public String certUri() {
        return null;
    }

    @Override
    public String[] origins() {
        return new String[0];
    }

    @Override
    public void registerMetricsLister(MetricsListener metricsListener) {
    }

    @Override
    public void setup(ServiceContext serviceContext) {
    }

    @Override
    public boolean validate(Map<String, Object> map) {
        String typeId = (String) map.remove(OnAccess.TYPE_ID);
        if(typeId==null) return false;
        TokenValidatorProvider.AuthVendor vendor = aMap.get(typeId);
        if(vendor==null) return false;
        return vendor.validate(map);
    }

    @Override
    public boolean upload(String query, byte[] bytes) {
        String[] typeName = query.split("#");
        TokenValidatorProvider.AuthVendor vendor = aMap.get(typeName[0]);
        if(vendor == null) return false;
        return vendor.upload(typeName[1],bytes);
    }

    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        authVendor.registerMetricsLister(metricsListener);
        authVendor.setup(serviceContext);
    }
    public void releaseAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        aMap.remove(authVendor.name());
    }
}
