package com.icodesoftware.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class GameObject {

    public List<GameStatsDelta> gameStatsDeltaList;
    public GameRating[] gameRatings;
    public GameObject(int capacity){
        gameStatsDeltaList = new ArrayList<>();
        gameRatings = new GameRating[capacity];
    }
    public void setup(DataBuffer spec){

    }
    public GameObject(){

    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray jo = new JsonArray();
        gameStatsDeltaList.forEach((g)->jo.add(g.toJson()));
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
        gameObject.gameStatsDeltaList = new ArrayList<>();

        return gameObject;
    }
}
