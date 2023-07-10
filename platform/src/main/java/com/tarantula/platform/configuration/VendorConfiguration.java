package com.tarantula.platform.configuration;


import com.icodesoftware.OnAccess;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

public class VendorConfiguration extends Application {

    private enum VendorId {AppleStore,GoogleStore,Amazon,Facebook,GooglePlay,MySQL}

    private String typeId;
    public VendorConfiguration(String typeId, ConfigurableObject configurableObject){
        super(configurableObject);
        this.typeId = typeId;
    }
    public String typeId(){
        return typeId;
    }
    public String name(){
        return this.toName(VendorId.values()[header.get("VendorId").getAsInt()]);
    }
    public String configurationFile(){
        return header.get("ConfigurationFile").getAsString();
    }

    public String description(){
        return header.get("Description").getAsString();
    }

    private String toName(VendorId vendorId){
        String vendorName = this.configurationName;
        switch (vendorId){
            case AppleStore:
                vendorName = OnAccess.APPLE_STORE;
                break;
            case GoogleStore:
                vendorName = OnAccess.GOOGLE_STORE;
                break;
            case Amazon:
                vendorName = OnAccess.AMAZON;
                break;
            case Facebook:
                vendorName = OnAccess.FACEBOOK;
                break;
            case GooglePlay:
                vendorName = OnAccess.GOOGLE_PLAY;
                break;
            case MySQL:
                vendorName = OnAccess.MYSQL;
                break;

        }
        return vendorName;
    }
}
