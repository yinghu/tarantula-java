package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.item.ItemPortableRegistry;

public class VendorConfiguration extends RecoverableObject {

    public VendorConfiguration(){}

    public VendorConfiguration(JsonObject payload){

    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.VENDOR_CONFIGURATION_CID;
    }


}
