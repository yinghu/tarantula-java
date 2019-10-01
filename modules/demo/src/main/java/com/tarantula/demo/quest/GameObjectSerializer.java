package com.tarantula.demo.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.util.OnApplicationSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu 9/30/2019
 */

public class GameObjectSerializer implements JsonSerializer<GameObject> {
    @Override
    public JsonElement serialize(GameObject demoObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new OnApplicationSerializer().serialize(demoObject,type,jsonSerializationContext);
        jo.add("gameObject",demoObject.setup(type,jsonSerializationContext));
        return jo;
    }
}
