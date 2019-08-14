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
public class CargoSerializer implements JsonSerializer<Cargo> {
    @Override
    public JsonElement serialize(Cargo cargo, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo =  (JsonObject) new GameComponentSerializer().serialize(cargo,type,jsonSerializationContext);
        jo.addProperty("score",cargo.dice1+cargo.dice2);
        jo.addProperty("dice1",cargo.dice1);
        jo.addProperty("dice2",cargo.dice2);
        return jo;
    }
}
