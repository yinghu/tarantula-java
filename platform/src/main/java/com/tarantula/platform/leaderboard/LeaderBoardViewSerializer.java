package com.tarantula.platform.leaderboard;

import com.google.gson.*;
import com.tarantula.platform.util.SystemUtil;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Updated by yinghu 5/4/2020
 */
public class LeaderBoardViewSerializer implements JsonSerializer<LeaderBoardView> {


    public JsonElement serialize(LeaderBoardView leaderBoard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",true);
        jo.addProperty("category",leaderBoard.category);
        jo.addProperty("classifier",leaderBoard.classifier);
        jo.addProperty("size",leaderBoard.size);
        JsonArray blist = new JsonArray();
        for(LeaderBoardView.EntryView e : leaderBoard.board){
                JsonObject b = new JsonObject();
                b.addProperty("rank",e.rank);
                b.addProperty("owner",e.owner);
                b.addProperty("value",e.value);
                b.addProperty("lastUpdated", SystemUtil.fromUTCMilliseconds(e.timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
                blist.add(b);

        }
        jo.add("board",blist);
        return jo;
    }
}
