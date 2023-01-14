package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.lobby.ArenaItem;


public class Arena {

    private ArenaItem arenaItem;

    public Arena(ArenaItem arenaItem){
        this.arenaItem = arenaItem;
    }
    public int xp(){
        return arenaItem.xp();
    }
    public int level(){
        return arenaItem.level();
    }
    public JsonObject toJson(){
        if(arenaItem==null) return new JsonObject();
        return arenaItem.toJson();
    }
}
