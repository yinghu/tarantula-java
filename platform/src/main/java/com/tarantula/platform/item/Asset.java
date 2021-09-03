package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.Map;

public class Asset extends ConfigurableObject{

    public Asset(){}

    public Asset(ConfigurableObject configurableObject){
        this.configurationType = configurableObject.configurationType;
        this.configurationTypeId = configurableObject.configurationTypeId;
        this.configurationName = configurableObject.configurationName;
        this.configurationCategory = configurableObject.configurationCategory;
        this.configurationVersion = configurableObject.configurationVersion;
        this.header = configurableObject.header;
        this.payload = configurableObject.payload;
        this.distributionKey(configurableObject.distributionKey());
    }

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
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("itemId", distributionKey());
        jsonObject.addProperty("configurationType", configurationType);
        jsonObject.addProperty("configurationTypeId", configurationTypeId);
        jsonObject.addProperty("configurationName", configurationName);
        jsonObject.addProperty("configurationCategory", configurationCategory);
        jsonObject.addProperty("configurationVersion", configurationVersion);
        jsonObject.add("header", header);
        jsonObject.add("payload", payload);
        return jsonObject;
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
    @Override
    public  <T extends Configurable> T setup(){
        return (T)this;
    }

}
