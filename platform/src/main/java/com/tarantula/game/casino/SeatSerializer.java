package com.tarantula.game.casino;

import com.google.gson.*;
import com.tarantula.game.GameComponentSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 4/23/2019
 */
public class SeatSerializer implements JsonSerializer<Seat> {
    @Override
    public JsonElement serialize(Seat position, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameComponentSerializer().serialize(position,type,jsonSerializationContext);
        jo.addProperty("bank",position.bank);
        jo.addProperty("occupied",position.occupied);
        jo.addProperty("dealing",position.dealing);
        jo.addProperty("wagered",position.wagered);
        jo.addProperty("wager", SystemUtil.toCreditsString(position.wager()));
        jo.addProperty("balance",position.balance());
        jo.addProperty("balanceAsString", SystemUtil.toCreditsString(position.balance()));
        jo.addProperty("onTurn",position.onTurn);
        return jo;
    }
}
