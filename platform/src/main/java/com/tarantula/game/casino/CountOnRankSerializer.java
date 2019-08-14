package com.tarantula.game.casino;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 4/25/2019
 */
public class CountOnRankSerializer implements JsonSerializer<CountOnRank> {


    public JsonElement serialize(CountOnRank countOnRank, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",countOnRank.name());
        jo.addProperty("rank",countOnRank.rank);
        jo.addProperty("count",countOnRank.count);
        return jo;
    }
}
