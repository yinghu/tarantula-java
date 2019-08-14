package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Configuration;


import java.lang.reflect.Type;

public class ConfigurationSerializer implements JsonSerializer<Configuration> {
    @Override
    public JsonElement serialize(Configuration configuration, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("tag",configuration.tag());
        jo.addProperty("type",configuration.type());
        configuration.properties().forEach((kv)->{
            jo.addProperty(kv.name(),kv.value());
        });
        return jo;
    }
}
