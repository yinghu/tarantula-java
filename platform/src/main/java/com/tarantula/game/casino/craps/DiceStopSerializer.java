package com.tarantula.game.casino.craps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/20/2019.
 */
public class DiceStopSerializer implements JsonSerializer<DiceStop> {

    @Override
    public JsonElement serialize(DiceStop diceStop, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",diceStop.name());
        jo.addProperty("stopNumber",diceStop.stop);
        jo.addProperty("dice1",diceStop.dice[0]);
        jo.addProperty("dice2",diceStop.dice[1]);
        jo.add("puck",new PuckSerializer().serialize(diceStop.puck,type,jsonSerializationContext));
        return jo;
    }
}
