package com.tarantula.platform.leaderboard;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;
import com.icodesoftware.util.TimeUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeaderBoardView implements JsonSerializable {
    public String category;
    public String classifier;
    public int size;
    public List<EntryView> board;

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("Successful",true);
        jo.addProperty("Category",category);
        jo.addProperty("Classifier",classifier);
        jo.addProperty("Size",size);
        JsonArray blist = new JsonArray();
        for(LeaderBoardView.EntryView e : board){
            blist.add(e.toJson());

        }
        jo.add("_entries",blist);
        return jo;
    }

    public static class EntryView implements JsonSerializable{
        public EntryView(int rank,String owner,double value,long timestamp){
            this.rank = rank;
            this.owner = owner;
            this.value = value;
            this.timestamp = timestamp;
        }
        public int rank;
        public String owner;
        public double value;
        public long timestamp;

        public JsonObject toJson(){
            JsonObject b = new JsonObject();
            b.addProperty("Rank",rank);
            b.addProperty("Owner",owner);
            b.addProperty("Value",value);
            b.addProperty("LastUpdated", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
            return b;
        }
    }
}
