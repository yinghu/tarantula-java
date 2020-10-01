package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.OnView;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 9/5/2019.
 */
public class OnViewSerializer implements JsonSerializer<OnView> {
    public JsonElement serialize(OnView descriptor, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("viewId",descriptor.viewId());
        if(descriptor.flag()!=null){
            jo.addProperty("flag",descriptor.flag());
        }
        if(descriptor.moduleFile()!=null){
            jo.addProperty("moduleFile",descriptor.moduleFile());
        }
        if(descriptor.moduleName()!=null){
            jo.addProperty("moduleName",descriptor.moduleName());
        }
        if(descriptor.moduleResourceFile()!=null){
            jo.addProperty("moduleResourceFile",descriptor.moduleResourceFile());
        }
        return jo;
    }
}
