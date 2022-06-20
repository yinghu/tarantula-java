package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.item.Asset;

import java.lang.reflect.Type;

public class AssetSerializer implements JsonSerializer<Asset> {

    public JsonElement serialize(Asset configurableObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new ConfigurableObjectSerializer().serialize(configurableObject,type,jsonSerializationContext).getAsJsonObject();
        return jsonObject;
    }
}
