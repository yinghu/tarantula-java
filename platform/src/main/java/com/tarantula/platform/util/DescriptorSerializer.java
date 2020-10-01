package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.Descriptor;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 5/25/2020
 */
public class DescriptorSerializer implements JsonSerializer<Descriptor> {
    public JsonElement serialize(Descriptor descriptor, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("type",descriptor.type());
        jo.addProperty("typeId",descriptor.typeId());
        jo.addProperty("name",descriptor.name());
        if(!descriptor.type().equals("lobby")){
            jo.addProperty("singleton",descriptor.singleton());
            jo.addProperty("tag",descriptor.tag());
            jo.addProperty("accessRank",descriptor.accessRank());
            jo.addProperty("applicationId",descriptor.distributionKey());
        }
        return jo;
    }
}
