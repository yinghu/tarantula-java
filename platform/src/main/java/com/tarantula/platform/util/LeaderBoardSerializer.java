package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.LeaderBoard;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Updated by yinghu 8/24/19
 */
public class LeaderBoardSerializer implements JsonSerializer<LeaderBoard> {


    public JsonElement serialize(LeaderBoard leaderBoard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();//new ResponseSerializer().serialize(leaderBoard,type,jsonSerializationContext);
        //jo.addProperty("label",leaderBoard.label());
        jo.addProperty("header",leaderBoard.header());
        jo.addProperty("name",leaderBoard.name());
        jo.addProperty("classifier",leaderBoard.classifier());
        jo.addProperty("category",leaderBoard.category());
        jo.addProperty("size",leaderBoard.size());
        JsonArray blist = new JsonArray();
        for(LeaderBoard.Entry e : leaderBoard.onBoard()){
            if(e.value()>0){
                JsonObject b = new JsonObject();
                b.addProperty("systemId",e.systemId());
                b.addProperty("value",e.value());
                b.addProperty("lastUpdated",SystemUtil.fromUTCMilliseconds(e.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
                blist.add(b);
            }
        }
        jo.add("board",blist);
        return jo;
    }
}
