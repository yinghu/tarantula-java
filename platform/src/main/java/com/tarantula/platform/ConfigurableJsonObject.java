package com.tarantula.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class ConfigurableJsonObject extends RecoverableObject {

    protected JsonArray configurableTemplate;
    @Override
    public JsonObject toJson(){
        JsonObject jsp = new JsonObject();
        if(configurableTemplate==null) return jsp;
        //configurableTemplate.forEach();
        return jsp;
    }
}
