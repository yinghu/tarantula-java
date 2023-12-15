package com.tarantula.platform.service;


import com.icodesoftware.OnAccess;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.HttpCaller;

import java.util.Map;

public class AuthObject implements TokenValidatorProvider.AuthVendor {

    protected static String ACCEPT = HttpCaller.ACCEPT;
    protected static String ACCEPT_JSON = HttpCaller.ACCEPT_JSON;
    protected static int TIMEOUT = HttpCaller.TIME_OUT;
    protected static String CONTENT_TYPE = HttpCaller.CONTENT_TYPE;
    protected static String CONTENT_FORM = HttpCaller.CONTENT_FORM;
    protected static String AUTHORIZATION = HttpCaller.AUTHORIZATION;
    protected final String typeId;

    protected   String clientId;

    protected ServiceContext serviceContext;
    protected MetricsListener metricsListener;
    protected MetricsListener applicationMetricsListener;
    protected TokenValidatorProvider tokenValidatorProvider;


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

    public <T extends Recoverable> boolean upload(String context,T content){
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
