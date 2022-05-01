package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class PlayerSavedGames{

    public List<SavedGame> savedGames;

    public PlayerSavedGames(List<SavedGame> savedGames){
        this.savedGames = savedGames;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray saves = new JsonArray();
        savedGames.forEach(save->saves.add(save.toJson()));
        jsonObject.add("savedGames",saves);
        return jsonObject;
    }
}
