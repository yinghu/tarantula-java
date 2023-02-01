package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

public class Seat extends GameObject {

    public boolean occupied;
    public synchronized JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jo = new JsonObject();
        jo.addProperty("index",this.stub);
        jo.addProperty("occupied",this.occupied);
        jo.addProperty("systemId",this.systemId);
        return jo;
    }
}
