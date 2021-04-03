package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.OnView;

import java.lang.reflect.Type;

public class OnViewSerializer implements JsonSerializer<OnView> {
    public JsonElement serialize(OnView descriptor, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("viewId",descriptor.viewId());
        if(descriptor.moduleContext()!=null){
            jo.addProperty("moduleContext",descriptor.moduleContext());
        }
        if(descriptor.moduleResourceFile()!=null){
            jo.addProperty("moduleResourceFile",descriptor.moduleResourceFile());
        }
        return jo;
    }
}
