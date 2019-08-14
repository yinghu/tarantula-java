package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.OnSession;


import java.lang.reflect.Type;

/**
 * Updated by yinghu on 10/7/2018.
 */
public class OnSessionSerializer implements JsonSerializer<OnSession>{
    public JsonElement serialize(OnSession presence, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jp = new JsonObject();
        jp.addProperty("successful",presence.successful());
        jp.addProperty("systemId",presence.systemId());
        jp.addProperty("stub",presence.stub());
        jp.addProperty("token",presence.token());
        jp.addProperty("ticket",presence.ticket());
        jp.addProperty("balance",String.format( "%.2f",presence.balance()));
        jp.addProperty("login",presence.login());
        return jp;
    }
}
