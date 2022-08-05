package com.tarantula.platform.configuration;

import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class GoogleStoreConfiguration extends Application {


    public GoogleStoreConfiguration(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public String name(){
        return header.get("Name").getAsString();
    }
    public String region(){
        return header.get("Region").getAsString();
    }
    public String bucket(){
        return header.get("Bucket").getAsString();
    }
    public String accessKeyId(){
        return header.get("AccessKeyId").getAsString();
    }
    public String secretAccessKey(){
        return header.get("SecretAccessKey").getAsString();
    }
}
