package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ConfigurableSetting {

    public String scope;
    public String type;//category
    public String description;
    public boolean rechargeable;
    public String version;

    public JsonArray properties;



    private JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("scope",scope);
        jsonObject.addProperty("category",type);
        jsonObject.addProperty("version",version);
        jsonObject.addProperty("description",description);
        jsonObject.addProperty("rechargeable",rechargeable);
        jsonObject.add("properties",properties);
        return jsonObject;
    }


    @Override
    public String toString(){
        return toJson().toString();
    }
}
