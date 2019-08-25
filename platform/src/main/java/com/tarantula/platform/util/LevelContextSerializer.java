package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.XP;
import com.tarantula.platform.leveling.LevelContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 8/24/19
 */
public class LevelContextSerializer implements JsonSerializer<LevelContext> {

    public JsonElement serialize(LevelContext levelContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject pc = (JsonObject)new ResponseSerializer().serialize(levelContext,type,jsonSerializationContext); //new JsonObject();
        if(levelContext.level!=null){
            pc.add("level",new XPLevelSerializer().serialize(levelContext.level,type,jsonSerializationContext));
        }
        if(levelContext.xp!=null){
            JsonArray xlist = new JsonArray();
            XPGainSerializer xs = new XPGainSerializer();
            for(XP x : levelContext.xp){
                xlist.add(xs.serialize(x,type,jsonSerializationContext));
            }
            pc.add("xp",xlist);
        }
        return pc;
    }
}
