package com.tarantula.platform.leaderboard;

import com.google.gson.*;
import com.icodesoftware.LeaderBoard;

import java.lang.reflect.Type;

public class LeaderBoardEntryDeserializer implements JsonDeserializer<LeaderBoardEntry> {

    @Override
    public LeaderBoardEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        return toEntry(jo);
    }
    private LeaderBoardEntry toEntry(JsonObject jo){
        String category = jo.get("1").getAsString();
        String classifier = jo.get("2").getAsString();
        int rank = jo.get("3").getAsInt();
        String owner = jo.get("4").getAsString();
        double value = jo.get("5").getAsDouble();
        long timestamp = jo.get("6").getAsLong();
        return new LeaderBoardEntry(classifier,category,rank,owner,value,timestamp);
    }
}
