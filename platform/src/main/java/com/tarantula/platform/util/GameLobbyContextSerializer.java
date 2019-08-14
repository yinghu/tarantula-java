package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.InstanceRegistry;

import com.tarantula.platform.playmode.GameDescriptor;
import com.tarantula.platform.playmode.GameLobbyContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 5/22/2018.
 */
public class GameLobbyContextSerializer implements JsonSerializer<GameLobbyContext> {

    public JsonElement serialize(GameLobbyContext gameContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new ResponseSerializer().serialize(gameContext,type,jsonSerializationContext);
        jo.addProperty("successful",gameContext.successful());
        jo.addProperty("command",gameContext.command());
        if(gameContext.gameList!=null){
            GameDescriptorSerializer ser = new GameDescriptorSerializer();
            JsonArray glist = new JsonArray();
            for(GameDescriptor game : gameContext.gameList){
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
