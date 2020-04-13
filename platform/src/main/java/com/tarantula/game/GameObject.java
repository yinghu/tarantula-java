package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

public class GameObject extends ResponseHeader {

    public Stub stub;

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
        }
        jo.add("stub",stub.toJson());
        return jo;
    }
}
