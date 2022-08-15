package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.Access;

import java.lang.reflect.Type;

public class AccessSerializer implements JsonSerializer<Access> {

    public JsonElement serialize(Access access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("login",access.login());
        jo.addProperty("emailAddress",access.emailAddress());
        jo.addProperty("activated",access.activated());
        jo.addProperty("role",access.role());
        jo.addProperty("validator",access.validator());
        jo.addProperty("primary",access.primary());
        jo.addProperty("revision",Long.toString(access.revision()));
        return jo;
    }
}
