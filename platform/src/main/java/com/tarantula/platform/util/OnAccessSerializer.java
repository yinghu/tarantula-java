package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.OnAccess;

import java.lang.reflect.Type;

public class OnAccessSerializer implements JsonSerializer<OnAccess> {

    public JsonElement serialize(OnAccess access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("timestamp",access.timestamp());
        jo.addProperty("accessKey",(String) access.property(OnAccess.ACCESS_KEY));
        jo.addProperty("accessId",(String) access.property(OnAccess.ACCESS_ID));
        jo.addProperty("oid",access.oid());
        //jo.addProperty("accessMode",access.accessMode());
        return jo;
    }
}
