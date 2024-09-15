package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.item.ConfigurableObject;


public class FileCredentialConfiguration extends CredentialConfiguration {

    private ServiceContext serviceContext;
    private static TarantulaLogger logger = JDKLogger.getLogger(FileCredentialConfiguration.class);
    private byte[] data;
    public FileCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
    }

    public FileCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,configurableObject.configurationName(),configurableObject);
        this.typeId = typeId;
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        this.serviceContext = serviceContext;
        Content content = serviceContext.deploymentServiceProvider().resource(header.get("File").getAsString());
        return content.existed();
    }

    public byte[] load(){
        return data;
        //Content content = serviceContext.deploymentServiceProvider().resource(header.get("File").getAsString());
        //return content.existed()?content.data():new byte[0];
    }


    @Override
    public boolean setup(ServiceContext serviceContext) {
        logger.warn(application.toString());
        String fileName = application.get("File").getAsString();
        data = serviceContext.node().homingAgent().onDownload(fileName);
        logger.warn(new String(data));
        return true;
    }
}
