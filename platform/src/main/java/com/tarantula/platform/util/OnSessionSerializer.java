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
        jp.addProperty("Successful",true);
        jp.addProperty("SystemId",presence.systemId());
        jp.addProperty("Stub",presence.stub());
        jp.addProperty("Token",presence.token());
        jp.addProperty("Ticket",presence.ticket());
        //jp.addProperty("balance",String.format( "%.2f",presence.balance()));
        jp.addProperty("Login",presence.login());
        //jp.addProperty("sessionCount",presence.version());
        return jp;
    }
}
