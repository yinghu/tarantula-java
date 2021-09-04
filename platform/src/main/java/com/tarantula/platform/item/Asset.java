package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.Map;

public class Asset extends ConfigurableObject{

    public Asset(){}

    public Asset(ConfigurableObject configurableObject){
        super(configurableObject);
    }

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
        return ItemPortableRegistry.ASSET_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = super.toJson();
        return jsonObject;
    }

    @Override
    public  <T extends Configurable> T setup(){
        return (T)this;
    }

}
