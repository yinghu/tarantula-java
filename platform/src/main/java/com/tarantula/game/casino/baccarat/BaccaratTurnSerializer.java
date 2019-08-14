package com.tarantula.game.casino.baccarat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class BaccaratTurnSerializer implements JsonSerializer<BaccaratTurn> {


    public JsonElement serialize(BaccaratTurn turn, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",turn.name());
        jo.addProperty("event",turn.event());
        //jo.addProperty("duration",turn.duration/1000);
        jo.addProperty("remainingTime", SystemUtil.remainingTimeAsString(turn.timestamp(),2));
        jo.addProperty("systemId",turn.systemId());
        jo.addProperty("seat",turn.seat);
        return jo;
    }
}
