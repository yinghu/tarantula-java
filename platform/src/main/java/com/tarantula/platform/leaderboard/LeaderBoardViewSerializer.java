package com.tarantula.platform.leaderboard;

import com.google.gson.*;
import com.icodesoftware.util.TimeUtil;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

public class LeaderBoardViewSerializer implements JsonSerializer<LeaderBoardView> {


    public JsonElement serialize(LeaderBoardView leaderBoard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("Successful",true);
        jo.addProperty("Category",leaderBoard.category);
        jo.addProperty("Classifier",leaderBoard.classifier);
        jo.addProperty("Size",leaderBoard.size);
        JsonArray blist = new JsonArray();
        for(LeaderBoardView.EntryView e : leaderBoard.board){
                JsonObject b = new JsonObject();
                b.addProperty("Rank",e.rank);
                b.addProperty("Owner",e.owner);
                b.addProperty("Value",e.value);
                b.addProperty("LastUpdated", TimeUtil.fromUTCMilliseconds(e.timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
                blist.add(b);

        }
        jo.add("_entries",blist);
        return jo;
    }
}
