package com.tarantula.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.util.OnApplicationSerializer;

import java.lang.reflect.Type;

public class DemoObjectSerializer implements JsonSerializer<DemoObject> {
    @Override
    public JsonElement serialize(DemoObject demoObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new OnApplicationSerializer().serialize(demoObject,type,jsonSerializationContext);
        jo.add("gameObject",demoObject.setup(type,jsonSerializationContext));
        return jo;
    }
}
