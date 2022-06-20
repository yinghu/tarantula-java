package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.item.ConfigurableObject;

import java.lang.reflect.Type;

public class ConfigurableObjectSerializer implements JsonSerializer<ConfigurableObject> {

    public JsonElement serialize(ConfigurableObject configurableObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("configurationType",configurableObject.configurationType());
        jsonObject.addProperty("configurationTypeId",configurableObject.configurationTypeId());
        jsonObject.addProperty("configurationName",configurableObject.configurationName());
        jsonObject.addProperty("configurationCategory",configurableObject.configurationCategory());
        jsonObject.addProperty("configurationVersion",configurableObject.configurationVersion());
        jsonObject.addProperty("itemId", configurableObject.distributionKey());
        jsonObject.addProperty("disabled",configurableObject.disabled());
        jsonObject.add("header",configurableObject.header());
        jsonObject.add("application",configurableObject.application());
        jsonObject.add("reference",configurableObject.reference());
        return jsonObject;
    }
}
