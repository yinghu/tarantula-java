package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.OnSession;


import java.lang.reflect.Type;

public class  OnSessionSerializer implements JsonSerializer<OnSession>{
    public JsonElement serialize(OnSession presence, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jp = new JsonObject();
        jp.addProperty("systemId",presence.systemId());
        jp.addProperty("stub",presence.stub());
        jp.addProperty("balance",String.format( "%.2f",presence.balance()));
        return jp;
    }
}
