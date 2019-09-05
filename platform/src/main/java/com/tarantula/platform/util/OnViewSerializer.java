package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.OnView;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 9/5/2019.
 */
public class OnViewSerializer implements JsonSerializer<OnView> {
    public JsonElement serialize(OnView descriptor, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("viewId",descriptor.viewId());
        if(descriptor.flag()!=null&&(!descriptor.flag().equals("n/a"))){
            jo.addProperty("flag",descriptor.flag());
        }
        if(descriptor.icon()!=null&&(!descriptor.icon().equals("n/a"))){
            jo.addProperty("icon",descriptor.icon());
        }
        if(descriptor.category()!=null&&(!descriptor.category().equals("n/a"))){
            jo.addProperty("category",descriptor.category());
        }
        if(descriptor.contentBaseUrl()!=null&&(!descriptor.contentBaseUrl().equals("n/a"))){
            jo.addProperty("contentBaseUrl",descriptor.contentBaseUrl());
        }
        if(descriptor.moduleFile()!=null&&(!descriptor.moduleFile().equals("n/a"))){
            jo.addProperty("moduleFile",descriptor.moduleFile());
        }
        if(descriptor.moduleName()!=null&&(!descriptor.moduleName().equals("n/a"))){
            jo.addProperty("moduleName",descriptor.moduleName());
        }
        if(descriptor.moduleResourceFile()!=null&&(!descriptor.moduleResourceFile().equals("n/a"))){
            jo.addProperty("moduleResourceFile",descriptor.moduleResourceFile());
        }
        return jo;
    }
}
