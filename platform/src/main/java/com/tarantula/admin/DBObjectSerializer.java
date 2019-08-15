package com.tarantula.admin;

import com.google.gson.*;
import com.tarantula.platform.util.OnApplicationSerializer;


import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 8/14/2019.
 */
public class DBObjectSerializer implements JsonSerializer<DBObject> {

    @Override
    public JsonElement serialize(DBObject sicBo, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new OnApplicationSerializer().serialize(sicBo,type,jsonSerializationContext);
        return jo;
    }
}
