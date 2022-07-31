package com.tarantula.platform.service;

import java.util.Map;

public class AmazonCloudServiceProvider extends AuthObject{

    public AmazonCloudServiceProvider(Map<String,AmazonAWSProvider> awsProviders) {
        super("google","","","","","",new String[0]);
    }

    public boolean upload(String name,byte[] content){
        return false;
    }

}
