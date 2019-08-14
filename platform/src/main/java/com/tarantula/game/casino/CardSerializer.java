package com.tarantula.game.casino;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class CardSerializer implements JsonSerializer<Card> {


    public JsonElement serialize(Card card, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",card.name);
        jo.addProperty("suit",card.suit.name());
        jo.addProperty("sequence",card.sequence);
        return jo;
    }
}
