package com.tarantula.platform.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


import java.lang.reflect.Type;

public class LeaderBoardEntrySerializer implements JsonSerializer<LeaderBoardEntry> {

    public JsonElement serialize(LeaderBoardEntry entry, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("1",entry.category());
        jsonObject.addProperty("2",entry.classifier());
        jsonObject.addProperty("3",entry.rank());
        jsonObject.addProperty("4",entry.owner());
        jsonObject.addProperty("5",entry.value());
        jsonObject.addProperty("6",entry.timestamp());
        return jsonObject;
    }
}
