package com.tarantula.game.casino.baccarat;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class BaccaratTieBetLineSerializer implements JsonSerializer<BaccaratTieBetLine> {
    @Override
    public JsonElement serialize(BaccaratTieBetLine tieBetLine, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("lineId",tieBetLine.stub());
        jo.addProperty("name",tieBetLine.name());
        return jo;
    }
}
