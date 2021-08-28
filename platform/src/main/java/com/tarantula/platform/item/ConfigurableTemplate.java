package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.RecoverableObject;

public class ConfigurableTemplate extends RecoverableObject implements Configuration {

    public String type;
    public String category;
    public String version;
    public int quantity;
    public String description;


    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type",type);
        jsonObject.addProperty("category",category);
        jsonObject.addProperty("version",version);
        jsonObject.addProperty("quantity",quantity);
        jsonObject.addProperty("description",description);
        jsonObject.add("itemList",(JsonArray)property("itemList"));
        return jsonObject;
    }
}
