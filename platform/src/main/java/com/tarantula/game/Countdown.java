package com.tarantula.game;

import com.google.gson.JsonObject;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Countdown {
    public long minutes;
    public long seconds;
    public int state;
    public int totalJoined;

    public Countdown(long remaining,int state,int totalJoined){
        this.minutes = remaining/60000;
        this.seconds = (remaining%60000)/1000;
        this.state = state;
        this.totalJoined = totalJoined;
    }

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("m",minutes);
        jo.addProperty("s",seconds);
        jo.addProperty("state",state);
        jo.addProperty("totalJoined",totalJoined);
        return jo;
    }
}
