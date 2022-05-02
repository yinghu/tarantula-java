package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class PlayerSavedGames{

    public String systemId;
    public String deviceId;
    public List<SavedGame> savedGames;

    public PlayerSavedGames(String systemId,String deviceId,List<SavedGame> savedGames){
        this.systemId = systemId;
        this.deviceId = deviceId;
        this.savedGames = savedGames;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray saves = new JsonArray();
        savedGames.forEach(save->{
            if(save.owner().equals(systemId)&&save.index().equals(deviceId)) jsonObject.add("currentSavedGame",save.toJson());
            saves.add(save.toJson());
        });
        jsonObject.add("savedGames",saves);
        return jsonObject;
    }
}
