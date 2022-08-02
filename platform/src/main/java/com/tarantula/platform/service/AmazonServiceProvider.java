package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public class AmazonServiceProvider extends AuthObject{

    private Map<String,AmazonAWSProvider> amazonAWSProviders;
    public AmazonServiceProvider(Map<String,AmazonAWSProvider> awsProviders) {
        super(OnAccess.AMAZON,"","","","","",new String[0]);
        this.amazonAWSProviders = awsProviders;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        amazonAWSProviders.forEach((k,v)->{
            v.registerMetricsLister(this.metricsListener);
            v.setup(serviceContext);
        });
    }

    public boolean upload(String name,byte[] content){
        String[] query = name.split("#");
        AmazonAWSProvider aws = amazonAWSProviders.get(query[0]);
        if(aws==null) return false;
        return aws.upload(query[1],content);
    }

}
