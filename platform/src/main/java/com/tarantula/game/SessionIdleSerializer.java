package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 3/13/2019.
 */
public class SessionIdleSerializer implements JsonSerializer<SessionIdle> {
    @Override
    public JsonElement serialize(SessionIdle sessionIdle, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameComponentSerializer().serialize(sessionIdle,type,jsonSerializationContext);
        //jo.addProperty("name",sessionIdle.name());
        //jo.addProperty("componentId",sessionIdle.componentId);
        //jo.addProperty("systemId",sessionIdle.systemId());
        jo.addProperty("stub",sessionIdle.stub());
        return jo;
    }
}
