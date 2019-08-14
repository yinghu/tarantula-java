package com.tarantula.game.casino.sicbo;

import com.google.gson.*;
import com.tarantula.game.GameSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/12/2019.
 */
public class SicBoSerializer implements JsonSerializer<SicBo> {

    @Override
    public JsonElement serialize(SicBo sicBo, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new GameSerializer().serialize(sicBo,type,jsonSerializationContext);
        sicBo.kv.forEach((k,v)->{
            jo.addProperty(k,v);
        });
        return jo;
    }
}
