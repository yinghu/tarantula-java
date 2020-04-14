package com.tarantula.game;

import com.google.gson.JsonObject;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Countdown {
    public long minutes = 0;
    public long seconds = 0;

    public Countdown(){}
    public Countdown(long remaining){
        minutes = remaining/60000;
        seconds = (remaining%60000)/1000;

    }


    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("m",minutes);
        jo.addProperty("s",seconds);
        return jo;
    }
}
