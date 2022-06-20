package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.item.Component;

import java.lang.reflect.Type;

public class ComponentSerializer implements JsonSerializer<Component> {

    public JsonElement serialize(Component configurableObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new ConfigurableObjectSerializer().serialize(configurableObject,type,jsonSerializationContext).getAsJsonObject();
        return jsonObject;
    }
}
