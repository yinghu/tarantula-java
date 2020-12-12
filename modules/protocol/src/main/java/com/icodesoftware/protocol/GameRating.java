package com.icodesoftware.protocol;

import com.google.gson.JsonObject;

public class GameRating {
    public int seat;
    public int rank;
    public double xp;

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("seat",seat);
        jsonObject.addProperty("rank",rank);
        jsonObject.addProperty("xp",xp);
        return jsonObject;
    }
}
