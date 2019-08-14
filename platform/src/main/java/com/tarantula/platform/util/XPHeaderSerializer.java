package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.leveling.XPHeader;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 5/14/2018.
 */
public class XPHeaderSerializer implements JsonSerializer<XPHeader> {
    @Override
    public JsonElement serialize(XPHeader xpHeader, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jb = new JsonObject();
        jb.addProperty("name",xpHeader.name);
        jb.addProperty("category",xpHeader.category);
        return jb;
    }
}
