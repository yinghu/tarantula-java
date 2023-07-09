package com.tarantula.platform.configuration;


import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class VendorConfiguration extends Application {

    private String typeId;
    public VendorConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }
    public String typeId(){
        return typeId;
    }
    public String name(){
        return this.configurationName;
    }
    public int vendorId(){
        return header.get("VendorId").getAsInt();
    }
    public String configurationFile(){
        return header.get("ConfigurationFile").getAsString();
    }

    public String description(){
        return header.get("Description").getAsString();
    }
}
