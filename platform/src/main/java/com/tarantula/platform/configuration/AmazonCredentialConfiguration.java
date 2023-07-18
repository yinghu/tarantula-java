package com.tarantula.platform.configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class AmazonCredentialConfiguration extends CredentialConfiguration {

    private S3Client s3Client;
    public AmazonCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.AMAZON,configurableObject);
    }

    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("S3Client",serviceContext.deploymentServiceProvider(),dataStore);
        s3Client = new S3Client(JsonUtil.parse(configurationObject.value()));
        return s3Client.validate(serviceContext);
    }

    public S3Client s3Client(){
        return s3Client;
    }
}
