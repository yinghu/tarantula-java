package com.tarantula.game;

import com.google.gson.JsonObject;

public class Stub {
    public String roomId;
    public int seat;
    public String tag;

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("seat",seat);
        jo.addProperty("roomId",roomId);
        jo.addProperty("tag",tag);
        return jo;
    }
}
