package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

import com.icodesoftware.OnAccess;

import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class AmazonCredentialConfiguration extends CredentialConfiguration {

    private S3Client s3Client;

    public AmazonCredentialConfiguration(String typeId, JsonObject payload){
        super(typeId,payload);
        this.name  = OnAccess.AMAZON;
    }

    public AmazonCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.AMAZON,configurableObject);
    }

    public S3Client s3Client(){
        return s3Client;
    }

    @Override
    public boolean setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Content content = super.content("S3Client");
        if(!content.existed()) return false;
        s3Client = new S3Client(JsonUtil.parse(content.data()));
        return  s3Client.validate(serviceContext);
    }
}
