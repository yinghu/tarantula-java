package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.Map;

public class Item extends ConfigurableObject{

    private ArrayList<ConfigurableObject> _reference;
    public Item(){}

    public Item(ConfigurableObject configurableObject){
        this.configurationType = configurableObject.configurationType;
        this.configurationTypeId = configurableObject.configurationTypeId;
        this.configurationName = configurableObject.configurationName;
        this.configurationCategory = configurableObject.configurationCategory;
        this.configurationVersion = configurableObject.configurationVersion;
        this.header = configurableObject.header;
        this.payload = configurableObject.payload;
        this.application = configurableObject.application;
        this.reference = configurableObject.reference;
        this.distributionKey(configurableObject.distributionKey());
    }

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
        JsonObject json = super.toJson();
        _reference.forEach((cob)->{
            json.add(cob.configurationName,cob.toJson());
        });
        return json;
    }

    @Override
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

    @Override
    public  <T extends Configurable> T setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionKey(je.getAsString());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                _reference.add(cob.setup());
            }
        }
        return (T)this;
    }
}
