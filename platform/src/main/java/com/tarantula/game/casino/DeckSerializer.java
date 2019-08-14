package com.tarantula.game.casino;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.game.GameComponentSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class DeckSerializer implements JsonSerializer<Deck> {
    @Override
    public JsonElement serialize(Deck deck, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameComponentSerializer().serialize(deck,type,jsonSerializationContext);
        jo.addProperty("cutter",deck.cutter);
        return jo;
    }
}
