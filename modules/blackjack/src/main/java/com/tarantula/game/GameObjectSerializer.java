package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 8/14/2019.
 */
public class GameObjectSerializer implements JsonSerializer<GameObject> {

    @Override
    public JsonElement serialize(GameObject gameObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",gameObject.name());
        jo.addProperty("label",gameObject.label());
        jo.addProperty("instanceId",gameObject.instanceId());
        jo.addProperty("successful",gameObject.successful());
        jo.add("gameObject",gameObject.setup(type,jsonSerializationContext));
        return jo;
    }
}
