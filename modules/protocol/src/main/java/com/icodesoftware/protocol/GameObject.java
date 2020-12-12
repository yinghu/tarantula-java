package com.icodesoftware.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;

public class GameObject {

    private ConcurrentHashMap<String,GameStatsDelta> gameStatsDeltaMap;
    private GameRating[] gameRatings;
    public GameObject(int capacity){
        gameStatsDeltaMap = new ConcurrentHashMap<>();
        gameRatings = new GameRating[capacity];
    }
    public GameObject(){

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
        return jsonObject;
    }
    public static GameObject toGameObject(JsonObject payload){
        GameObject gameObject = new GameObject();
        //gameObject.gameStatsDeltaList = new ArrayList<>();

        return gameObject;
    }
}
