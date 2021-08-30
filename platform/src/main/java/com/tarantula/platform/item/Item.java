package com.tarantula.platform.item;

import com.google.gson.JsonObject;

import java.util.Map;

public class Item extends ConfigurableObject{


    @Override
    public Map<String,Object> toMap(){
        super.toMap();
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
    public boolean configureAndValidate(byte[] data){
        return super.configureAndValidate(data);
    }
}
