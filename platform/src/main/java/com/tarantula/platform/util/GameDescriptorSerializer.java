package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.playmode.GameDescriptor;

import java.lang.reflect.Type;

/**
 * updated by yinghu on 5/29/2019.
 */
public class GameDescriptorSerializer implements JsonSerializer<GameDescriptor> {

    public JsonElement serialize(GameDescriptor game, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new DescriptorSerializer().serialize(game.descriptor,type,jsonSerializationContext);
        if(game.configuration!=null){
            jo.add("configuration",new ConfigurationSerializer().serialize(game.configuration,type,jsonSerializationContext));
        }
        return jo;
    }
}
