package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.XP;
import com.tarantula.platform.leveling.LevelContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 3/5/2018.
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
        if(levelContext.headers!=null){
            JsonArray hlist = new JsonArray();
            XPHeaderSerializer headerSerializer = new XPHeaderSerializer();
            levelContext.headers.forEach((xh)->{
                hlist.add(headerSerializer.serialize(xh,type,jsonSerializationContext));
            });
            pc.add("headers",hlist);
        }
        return pc;
    }
}
