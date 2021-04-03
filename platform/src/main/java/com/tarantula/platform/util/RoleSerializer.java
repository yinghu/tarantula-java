package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.Access;

import java.lang.reflect.Type;

public class RoleSerializer implements JsonSerializer<Access.Role> {

    public JsonElement serialize(Access.Role access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",access.name());
        return jo;
    }
}
