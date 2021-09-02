package com.tarantula.platform.item;

import com.google.gson.JsonObject;

import java.util.Map;

public class Item extends ConfigurableObject{


    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        properties.put(HEADER_KEY,header.toString());
        properties.put(PAYLOAD_KEY,payload.toString());
        properties.put(APPLICATION_KEY,application.toString());
        properties.put(REFERENCE_KEY,reference.toString());
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
        return ItemPortableRegistry.ITEM_CID;
    }
    @Override
    public JsonObject toJson(){
       return super.toJson();
    }
    public boolean configureAndValidate(JsonObject config){
        if(!config.has("header")||!config.has("payload")||!config.has("application")||!config.has("reference")){
            return false;
        }
        this.header = config.getAsJsonObject("header");
        this.payload = config.getAsJsonObject("payload");
        this.application = config.getAsJsonObject("application");
        this.reference = config.getAsJsonArray("reference");
        return true;
    }
}
