package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.presence.saves.SavedGame;

import java.util.List;

public class PlayerSavedGames{

    public PlatformGameServiceProvider gameServiceProvider;
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
        jsonObject.addProperty("Successful",true);
        JsonArray saves = new JsonArray();
        savedGames.forEach(save->{
            if(save.owner().equals(systemId) && save.index().equals(deviceId)) {
                jsonObject.add("_currentSavedGame",save.toJson());
            }
            saves.add(save.toJson());
        });
        jsonObject.add("_savedGames",saves);
        return jsonObject;
    }
}
