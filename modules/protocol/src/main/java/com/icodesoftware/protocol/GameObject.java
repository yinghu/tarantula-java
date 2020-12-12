package com.icodesoftware.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;

public class GameObject {

    private ConcurrentHashMap<String,GameStatsDelta> gameStatsDeltaMap;
    private ConcurrentHashMap<Integer,GameItem> gameItemMap;
    private GameRating[] gameRatings;
    public GameObject(int capacity){
        gameStatsDeltaMap = new ConcurrentHashMap<>();
        gameRatings = new GameRating[capacity];
        gameItemMap = new ConcurrentHashMap<>();
    }
    public void gameItem(GameItem gameItem){
        gameItemMap.put(gameItem.sequence,gameItem);
    }
    public GameItem gameItem(int sequence){
        return gameItemMap.get(sequence);
    }
    public GameRating update(int seat){
        if(gameRatings[seat]==null){
            gameRatings[seat]=new GameRating(seat);
        }
        return gameRatings[seat];
    }
    public void update(GameStatsDelta delta){
        GameStatsDelta ex = gameStatsDeltaMap.putIfAbsent(delta.toString(),delta);
        if(ex!=null){
            ex.delta = ex.delta+delta.delta;
        }
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray jo = new JsonArray();
        gameStatsDeltaMap.forEach((k,g)->jo.add(g.toJson()));
        jsonObject.add("stats",jo);
        JsonArray jr = new JsonArray();
        for(int i=0;i<gameRatings.length;i++){
            if(gameRatings[i]!=null){
                jr.add(gameRatings[i].toJson());
            }
        }
        jsonObject.add("ratings",jr);
        JsonArray jg = new JsonArray();
        gameItemMap.forEach((k,v)->jg.add(v.toJson()));
        jsonObject.add("items",jg);
        return jsonObject;
    }
}
