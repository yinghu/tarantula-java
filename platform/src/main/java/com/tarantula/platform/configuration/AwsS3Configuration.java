package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class AwsS3Configuration extends Application {

    private String typeId;

    public AwsS3Configuration(String typeId,ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }

    public String typeId(){
        return typeId;
    }

    public String name(){
        return OnAccess.AMAZON;
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
