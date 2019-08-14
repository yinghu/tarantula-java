package com.tarantula.game.casino.craps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class PuckSerializer implements JsonSerializer<Puck> {

    @Override
    public JsonElement serialize(Puck puck, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",puck.name());
        jo.addProperty("on",puck.on);
        jo.addProperty("point",puck.point);
        return jo;
    }
}
