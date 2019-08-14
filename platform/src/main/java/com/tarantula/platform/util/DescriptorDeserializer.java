package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Descriptor;
import com.tarantula.platform.DeploymentDescriptor;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 10/7/2018.
 */
public class DescriptorDeserializer implements JsonDeserializer<Descriptor> {

    public Descriptor deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        Descriptor desc = new DeploymentDescriptor();
        desc.singleton(jo.get("singleton").getAsBoolean());
        desc.accessMode(jo.get("accessMode").getAsInt());
        if(jo.has("tag")){
            desc.tag(jo.get("tag").getAsString());
        }
        if(jo.has("subtypeId")){
            desc.subtypeId(jo.get("subtypeId").getAsString());
        }
        if(jo.has("applicationId")){
            desc.applicationId(jo.get("applicationId").getAsString());
        }
        if(jo.has("typeId")){
            desc.typeId(jo.get("typeId").getAsString());
        }
        return desc;
    }
}
