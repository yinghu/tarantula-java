package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashMap;
import java.util.Map;

public class ConfigurableTemplate extends RecoverableObject implements Configuration {

    public String type;
    public String category;
    public String version;
    public String description;
    public String name;
    public HashMap<String,ConfigurableSetting> settings = new HashMap<>();


    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",type);
        properties.put("2",category);
        properties.put("3",version);
        properties.put("4",description);
        properties.put("5",name);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String) properties.get("1");
        this.category = (String) properties.get("2");
        this.version = (String) properties.get("3");
        this.description = (String) properties.get("4");
        this.name = (String) properties.get("5");
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_TEMPLATE;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type",type);
        jsonObject.addProperty("category",category);
        jsonObject.addProperty("version",version);
        jsonObject.addProperty("description",description);
        jsonObject.addProperty("name",name);
        jsonObject.add("itemList",(JsonArray)property("itemList"));
        return jsonObject;
    }
}
