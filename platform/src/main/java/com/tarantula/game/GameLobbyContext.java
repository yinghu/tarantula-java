package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.concurrent.ConcurrentHashMap;

public class GameLobbyContext extends ResponseHeader {
    public ConcurrentHashMap<Integer,GameLobby> gameLobbyList;
    public int page;
    public int checkEnabledLobbyCount(){
        int[] mc = {0};
        gameLobbyList.forEach((k,a)->{
            if(!a.lobby.disabled()){
                mc[0]++;
            }
        });
        return mc[0];
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        GameLobby gameLobby = gameLobbyList.get(page);
        if(gameLobby==null){
            page = 0;
            jsonObject.addProperty("successful",false);
            jsonObject.addProperty("message","no more game lobby");
            return jsonObject;
        }
        jsonObject.addProperty("successful",this.successful);
        jsonObject.addProperty("lobbySize",gameLobbyList.size());
        jsonObject.addProperty("index",page);
        jsonObject.add("lobby",gameLobby.toJson());
        return jsonObject;
    }
}
