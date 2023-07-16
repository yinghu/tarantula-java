package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;

import com.tarantula.platform.item.ConfigurableObject;

public class AmazonCredentialConfiguration extends CredentialConfiguration {


    public AmazonCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.AMAZON,configurableObject);
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
