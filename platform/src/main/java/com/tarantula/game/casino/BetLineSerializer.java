package com.tarantula.game.casino;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.game.GameComponentSerializer;
import com.tarantula.platform.util.SystemUtil;


import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/11/2019.
 */
public class BetLineSerializer implements JsonSerializer<BetLine> {

    @Override
    public JsonElement serialize(BetLine betLine, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameComponentSerializer().serialize(betLine,type,jsonSerializationContext);
        jo.addProperty("betLineId",betLine.stub());
        jo.addProperty("wager", SystemUtil.toCreditsString(betLine.wager()));
        jo.addProperty("totalWager",SystemUtil.toCreditsString(betLine.balance()));
        jo.addProperty("x",betLine.x);
        jo.addProperty("y",betLine.y);
        return jo;
    }
}
