package com.tarantula.game.casino;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.game.GameComponentSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 3/1/2019.
 */
public class CashInBalanceSerializer implements JsonSerializer<CashInBalance> {
    @Override
    public JsonElement serialize(CashInBalance cashInBalance, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameComponentSerializer().serialize(cashInBalance,type,jsonSerializationContext);
        jo.addProperty("balance",cashInBalance.balance());
        jo.addProperty("balanceAsString", SystemUtil.toCreditsString(cashInBalance.balance()));
        return jo;
    }
}
