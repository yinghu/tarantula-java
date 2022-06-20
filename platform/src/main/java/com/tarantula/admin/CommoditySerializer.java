package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.item.Commodity;

import java.lang.reflect.Type;

public class CommoditySerializer implements JsonSerializer<Commodity> {

    public JsonElement serialize(Commodity configurableObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new ConfigurableObjectSerializer().serialize(configurableObject,type,jsonSerializationContext).getAsJsonObject();
        return jsonObject;
    }
}
