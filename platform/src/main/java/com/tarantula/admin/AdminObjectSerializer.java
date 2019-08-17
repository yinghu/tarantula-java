package com.tarantula.admin;

import com.google.gson.*;
import com.tarantula.platform.util.OnApplicationSerializer;


import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 8/14/2019.
 */
public class AdminObjectSerializer implements JsonSerializer<AdminObject> {

    @Override
    public JsonElement serialize(AdminObject adminObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new OnApplicationSerializer().serialize(adminObject,type,jsonSerializationContext);
        jo.add("payload",adminObject.setup(type,jsonSerializationContext));
        return jo;
    }
}
