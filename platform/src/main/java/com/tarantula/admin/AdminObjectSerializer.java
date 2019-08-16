package com.tarantula.admin;

import com.google.gson.*;
import com.tarantula.platform.util.OnApplicationSerializer;


import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 8/14/2019.
 */
public class AdminObjectSerializer implements JsonSerializer<AdminObject> {

    @Override
    public JsonElement serialize(AdminObject sicBo, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new OnApplicationSerializer().serialize(sicBo,type,jsonSerializationContext);
        return jo;
    }
}
