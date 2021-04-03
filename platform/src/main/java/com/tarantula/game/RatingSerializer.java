package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;


public class RatingSerializer implements JsonSerializer<Rating> {

    public JsonElement serialize(Rating response, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("rank",response.rank);
        jo.addProperty("xp",response.xp);
        jo.addProperty("elo",response.elo);
        jo.addProperty("successful",true);
        return jo;
    }
}
