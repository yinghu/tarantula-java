package com.tarantula.game.scc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.game.GameComponentSerializer;


import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 3/13/2019.
 */
public class DiceSideSerializer implements JsonSerializer<DiceSide> {
    @Override
    public JsonElement serialize(DiceSide diceSide, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo =  (JsonObject) new GameComponentSerializer().serialize(diceSide,type,jsonSerializationContext);
        jo.addProperty("rank",diceSide.released?diceSide.rank:0);
        jo.addProperty("released",diceSide.released);
        return jo;
    }
}
