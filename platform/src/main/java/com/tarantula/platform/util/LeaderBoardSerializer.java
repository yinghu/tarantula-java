package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.LeaderBoard;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Updated by yinghu 6/15/2018
 */
public class LeaderBoardSerializer implements JsonSerializer<LeaderBoard> {


    public JsonElement serialize(LeaderBoard leaderBoard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("header",leaderBoard.leaderBoardHeader());
        jo.addProperty("name",leaderBoard.name());
        jo.addProperty("classifier",leaderBoard.classifier());
        jo.addProperty("category",leaderBoard.category());
        jo.addProperty("size",leaderBoard.size());
        JsonArray blist = new JsonArray();
        for(LeaderBoard.Entry e : leaderBoard.list()){
            if(e.value()>0){
                JsonObject b = new JsonObject();
                b.addProperty("systemId",e.systemId());
                b.addProperty("value",e.value());
                b.addProperty("lastUpdated",SystemUtil.fromUTCMilliseconds(e.timestamp()).format(DateTimeFormatter.ISO_LOCAL_DATE));
                blist.add(b);
            }
        }
        jo.add("board",blist);
        return jo;
    }
}
