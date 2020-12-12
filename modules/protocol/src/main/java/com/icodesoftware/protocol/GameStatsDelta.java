package com.icodesoftware.protocol;

import com.google.gson.JsonObject;

public class GameStatsDelta {
    public int seat;
    public String category;
    public double delta;

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("seat",seat);
        jsonObject.addProperty("category",category);
        jsonObject.addProperty("delta",delta);
        return jsonObject;
    }
}
