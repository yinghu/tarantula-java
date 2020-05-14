package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Access;
import com.tarantula.OnAccess;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 5/13/2020
 */
public class AccessSerializer implements JsonSerializer<Access> {

    public JsonElement serialize(Access access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("login",access.login());
        jo.addProperty("emailAddress",access.emailAddress());
        jo.addProperty("role",access.role());
        jo.addProperty("validator",access.validator());
        return jo;
    }
}
