package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.leaderboard.LeaderBoardRegistry;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class LeaderBoardRegistrySerializer implements JsonSerializer<LeaderBoardRegistry> {


    public JsonElement serialize(LeaderBoardRegistry leaderBoard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("header",leaderBoard.header);
        jo.addProperty("name",leaderBoard.name);
        jo.addProperty("size",leaderBoard.size);
        JsonArray clist = new JsonArray();
        leaderBoard.classifierList.forEach((c)->{
            clist.add(c);
        });
        jo.add("classifiers",clist);
        JsonArray tlist = new JsonArray();
        leaderBoard.categoryList.forEach((t)->{
            tlist.add(t);
        });
        jo.add("categories",tlist);
        return jo;
    }
}
