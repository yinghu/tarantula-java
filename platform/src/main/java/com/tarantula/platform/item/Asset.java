package com.tarantula.platform.item;

import com.google.gson.JsonObject;

import java.util.Map;

public class Asset extends ConfigurableObject{

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        properties.put(HEADER_KEY,header.toString());
        properties.put(PAYLOAD_KEY,payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.ASSET_CID;
    }

    @Override
    public boolean configureAndValidate(JsonObject config){
        if(!config.has("header")||!config.has("payload")){
            return false;
        }
        this.header = config.getAsJsonObject("header");
        this.payload = config.getAsJsonObject("payload");
        return true;
    }

}
