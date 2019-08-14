package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Configuration;
import com.tarantula.platform.ApplicationConfiguration;
import java.lang.reflect.Type;

/**
 * Updated by yinghu on 7/12/19
 */
public class ConfigurationDeserializer implements JsonDeserializer<Configuration> {

    public Configuration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        Configuration desc = new ApplicationConfiguration();
        if(jo.has("tag")){
            desc.type(jo.get("tag").getAsString());
        }
        if(jo.has("type")){
            desc.type(jo.get("type").getAsString());
        }
        jo.entrySet().forEach((e)->{
            desc.configure(e.getKey(),e.getValue().getAsString());
        });
        return desc;
    }
}
