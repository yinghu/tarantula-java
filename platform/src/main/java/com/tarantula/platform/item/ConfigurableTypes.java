package com.tarantula.platform.item;

import com.google.gson.JsonArray;

import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;

import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;

public class ConfigurableTypes extends RecoverableObject implements Configuration {

    private static String ITEM_LIST = "itemList";

    private ConcurrentHashMap<String,ConfigurableType> types = new ConcurrentHashMap<>();

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        JsonArray items = new JsonArray();
        types.forEach((k,v)-> items.add(v.toJson()));
        jsonObject.add(ITEM_LIST,items);
        return jsonObject;
    }

    public List<ConfigurableType> toTypes(){
        ArrayList<ConfigurableType> list = new ArrayList<>();
        types.forEach((c,t)->list.add(t));
        return list;
    }

    public boolean addType(ConfigurableType type){
        return types.putIfAbsent(type.name(),type)==null;
    }
    public ConfigurableType type(String name){
        return types.get(name);
    }

}
