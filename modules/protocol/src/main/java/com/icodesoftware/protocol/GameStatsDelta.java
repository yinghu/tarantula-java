package com.icodesoftware.protocol;

import com.google.gson.JsonObject;

public class GameStatsDelta {
    public int seat;
    public String category;
    public double delta;

    public GameStatsDelta(int seat,String category,double delta){
        this.seat = seat;
        this.category = category;
        this.delta = delta;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("seat",seat);
        jsonObject.addProperty("category",category);
        jsonObject.addProperty("delta",delta);
        return jsonObject;
    }
    public String toString(){
        return "seat-"+seat+category;
    }
}
