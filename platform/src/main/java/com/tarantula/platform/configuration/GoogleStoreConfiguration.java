package com.tarantula.platform.configuration;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class GoogleStoreConfiguration extends Application {

    private String typeId;
    public GoogleStoreConfiguration(String typeId,ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }


    public String typeId(){
        return typeId;
    }

    public String name(){
        return OnAccess.GOOGLE_STORE;
    }

    public String packageName(){
        return header.get("PackageName").getAsString();
    }
    public String secretKey(){
        return header.get("SecretKey").getAsString();
    }

}
