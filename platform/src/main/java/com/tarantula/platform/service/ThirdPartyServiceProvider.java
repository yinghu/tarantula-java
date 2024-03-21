package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThirdPartyServiceProvider implements AuthVendorRegistry {

    private final ConcurrentHashMap<String, TokenValidatorProvider.AuthVendor> aMap;
    private final String name;
    private MetricsListener metricsListener;
    private ServiceContext serviceContext;

    public ThirdPartyServiceProvider(String name){
        this.name = name;
        this.aMap = new ConcurrentHashMap<>();
        this.metricsListener = (n,v)->{};
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
    public void registerMetricsLister(MetricsListener metricsListener) {
        if(metricsListener == null) return;
        this.metricsListener = metricsListener;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        aMap.forEach((k,v)->{
            v.registerMetricsLister(this.metricsListener);
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

    @Override
    public <T extends Recoverable> boolean upload(String query, T content) {
        String[] typeName = query.split("#");
        TokenValidatorProvider.AuthVendor vendor = aMap.get(typeName[0]);
        if(vendor == null) return false;
        return vendor.upload(typeName[1],content);
    }

    @Override
    public byte[] download(String query){
        String[] typeName = query.split("#");
        TokenValidatorProvider.AuthVendor vendor = aMap.get(typeName[0]);
        if(vendor == null) return new byte[0];
        return vendor.download(typeName[1]);
    }


    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        authVendor.setup(serviceContext);
        authVendor.registerMetricsLister(this.metricsListener);
        aMap.put(authVendor.typeId(),authVendor);
    }
    public void releaseAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        aMap.remove(authVendor.name());
    }
}
