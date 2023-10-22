package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Arena;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.lobby.ArenaItem;


public class GameArena extends RecoverableObject implements Arena {

    private ArenaItem arenaItem;

    public GameArena(ArenaItem arenaItem){
        this.arenaItem = arenaItem;
    }
    public int xp(){
        return arenaItem.xp();
    }
    public int level(){
        return arenaItem.level();
    }
    public String name(){
        return arenaItem.name();
    }
    public JsonObject toJson(){
        if(arenaItem==null) return new JsonObject();
        return arenaItem.toJson();
    }
}
