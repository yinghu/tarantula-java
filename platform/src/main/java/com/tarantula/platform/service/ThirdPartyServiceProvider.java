package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThirdPartyServiceProvider implements AuthVendorRegistry {

    public static final String AMAZON = "amazon";
    //public static final String

    private final ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> aMap;
    private final String name;
    private MetricsListener metricsListener;
    private ServiceContext serviceContext;

    public ThirdPartyServiceProvider(String name, List<TokenValidatorProvider.AuthVendor> preload){
        this.name = name;
        this.aMap = new ConcurrentHashMap<>();
        preload.forEach((v)-> aMap.put(v.name(),v));
    }

    public String typeId(){
        return name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String clientId() {
        throw new RuntimeException("Operation not support");
    }

    @Override
    public String clientId(String typeId) {
        TokenValidatorProvider.AuthVendor vendor = aMap.get(typeId);
        if(vendor == null) throw new RuntimeException("No auth vendor associated with ["+typeId+"]");
        return vendor.clientId();
    }


    @Override
    public void registerMetricsLister(MetricsListener metricsListener) {
        this.metricsListener = metricsListener;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        aMap.forEach((k,v)->{
            MetricsListener _m = serviceContext.deploymentServiceProvider().metricsListener(k);
            v.registerMetricsLister(_m);
            v.setup(this.serviceContext);
        });
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
        authVendor.setup(serviceContext);
        aMap.put(authVendor.typeId(),authVendor);
    }
    public void releaseAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        aMap.remove(authVendor.name());
    }
}
