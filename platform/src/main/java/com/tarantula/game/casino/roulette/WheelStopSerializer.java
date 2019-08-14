package com.tarantula.game.casino.roulette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/11/2019.
 */
public class WheelStopSerializer implements JsonSerializer<WheelStop> {

    @Override
    public JsonElement serialize(WheelStop wheelStop, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",wheelStop.name());
        jo.addProperty("stopNumber",wheelStop.symbol);
        jo.addProperty("stopIndex",wheelStop.subscript);
        jo.addProperty("color",wheelStop.color);
        return jo;
    }
}
