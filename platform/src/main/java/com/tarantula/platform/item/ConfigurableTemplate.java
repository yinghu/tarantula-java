package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashMap;

public class ConfigurableTemplate extends RecoverableObject implements Configuration {

    public String type;
    public String category;
    public String version;
    public String description;
    public String name;
    public HashMap<String,ConfigurableSetting> settings = new HashMap<>();

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
