package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.item.Item;

import java.lang.reflect.Type;

public class ItemSerializer implements JsonSerializer<Item> {

    public JsonElement serialize(Item configurableObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new ConfigurableObjectSerializer().serialize(configurableObject,type,jsonSerializationContext).getAsJsonObject();
        return jsonObject;
    }
}
