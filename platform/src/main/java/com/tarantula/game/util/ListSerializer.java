package com.tarantula.game.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class ListSerializer{

    public static JsonObject toJson(List<Long> list){
        JsonObject resp = new JsonObject();
        JsonArray alist = new JsonArray();
        list.forEach(v->alist.add(v));
        resp.addProperty("Successful",true);
        resp.add("_list",alist);
        return resp;
    }
}
