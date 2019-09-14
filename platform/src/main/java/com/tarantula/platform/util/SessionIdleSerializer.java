package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.SessionIdle;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 9/13/2019.
 */
public class SessionIdleSerializer implements JsonSerializer<SessionIdle> {
    @Override
    public JsonElement serialize(SessionIdle sessionIdle, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new ResponseSerializer().serialize(sessionIdle,type,jsonSerializationContext);
        jo.addProperty("systemId",sessionIdle.systemId());
        jo.addProperty("stub",sessionIdle.stub());
        jo.addProperty("instanceId",sessionIdle.instanceId());
        return jo;
    }
}
