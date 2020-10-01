package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.InstanceRegistry;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 2/19/2019.
 */
public class InstanceRegistrySerializer implements JsonSerializer<InstanceRegistry> {
    @Override
    public JsonElement serialize(InstanceRegistry instanceRegistry, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("capacity",instanceRegistry.capacity());
        jo.addProperty("count",instanceRegistry.count(0));
        jo.addProperty("instanceId",instanceRegistry.distributionKey());
        jo.addProperty("applicationId",instanceRegistry.applicationId());
        return jo;
    }
}
