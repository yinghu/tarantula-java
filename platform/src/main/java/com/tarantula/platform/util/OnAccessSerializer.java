package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.OnAccess;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 10/24/2018.
 */
public class OnAccessSerializer implements JsonSerializer<OnAccess> {

    public JsonElement serialize(OnAccess access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("timestamp",access.timestamp());
        jo.addProperty("accessKey",access.accessKey());
        jo.addProperty("accessId",access.accessId());
        jo.addProperty("oid",access.oid());
        jo.addProperty("applicationId",access.applicationId());
        jo.addProperty("accessMode",access.accessMode());
        access.header().forEach((String k,String v)-> jo.addProperty(k,v));
        return jo;
    }
}
