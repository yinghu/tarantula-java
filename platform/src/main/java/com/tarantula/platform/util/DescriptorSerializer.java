package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Descriptor;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 7/16/2019.
 */
public class DescriptorSerializer implements JsonSerializer<Descriptor> {
    public JsonElement serialize(Descriptor descriptor, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("singleton",descriptor.singleton());
        jo.addProperty("deployCode",descriptor.deployCode());
        jo.addProperty("tag",descriptor.tag());
        jo.addProperty("type",descriptor.type());
        jo.addProperty("typeId",descriptor.typeId());
        jo.addProperty("subtypeId",descriptor.subtypeId());
        jo.addProperty("category",descriptor.category());
        jo.addProperty("capacity",descriptor.capacity());
        jo.addProperty("name",descriptor.name());
        jo.addProperty("description",descriptor.description());
        jo.addProperty("icon",descriptor.icon());
        jo.addProperty("viewId",descriptor.viewId());
        jo.addProperty("applicationId",descriptor.distributionKey());
        jo.addProperty("accessMode",descriptor.accessMode());
        jo.addProperty("entryCost",descriptor.entryCost());
        jo.addProperty("entryCostAsString",SystemUtil.toCreditsString(descriptor.entryCost()));
        jo.addProperty("responseLabel",descriptor.responseLabel());
        jo.addProperty("tournamentEnabled",descriptor.tournamentEnabled());
        return jo;
    }
}
