package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.RecoverableObject;


public class ItemCategory extends RecoverableObject implements Configuration {
    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("description",(String)property("description"));
        jsonObject.addProperty("category",(String)property("category"));
        jsonObject.add("itemList",(JsonArray)property("itemList"));
        return jsonObject;
    }

}
