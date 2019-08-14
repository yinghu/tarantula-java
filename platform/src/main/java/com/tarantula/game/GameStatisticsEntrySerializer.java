package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.util.SystemUtil;


import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 3/13/2019.
 */
public class GameStatisticsEntrySerializer implements JsonSerializer<GameStatisticsEntry> {
    @Override
    public JsonElement serialize(GameStatisticsEntry gameStatisticsEntry, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameComponentSerializer().serialize(gameStatisticsEntry,type,jsonSerializationContext);
        jo.addProperty("key",gameStatisticsEntry.key);
        jo.addProperty("value", SystemUtil.toCreditsString(gameStatisticsEntry.value));
        return jo;
    }
}
