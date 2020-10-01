package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.Descriptor;
import com.tarantula.InstanceRegistry;
import com.tarantula.platform.module.LobbyContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 5/25/2020
 */
public class LobbyContextSerializer implements JsonSerializer<LobbyContext> {

    public JsonElement serialize(LobbyContext gameContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new ResponseSerializer().serialize(gameContext,type,jsonSerializationContext);
        if(gameContext.gameList!=null){
            DescriptorSerializer ser = new DescriptorSerializer();
            JsonArray glist = new JsonArray();
            for(Descriptor game : gameContext.gameList){
                glist.add(ser.serialize(game,type,jsonSerializationContext));
            }
            jo.add("gameList",glist);
        }
        if(gameContext.onList!=null){
            InstanceRegistrySerializer irs = new InstanceRegistrySerializer();
            JsonArray ji = new JsonArray();
            for(InstanceRegistry ir: gameContext.onList){
                ji.add(irs.serialize(ir,type,jsonSerializationContext));
            }
            jo.add("onLobby",ji);
        }
        return jo;
    }
}
