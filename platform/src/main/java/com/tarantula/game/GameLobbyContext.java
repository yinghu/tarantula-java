package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class GameLobbyContext extends ResponseHeader {
    public List<GameLobby> gameLobbyList;
    public int page;
    public int checkEnabledLobbyCount(){
        int[] mc = {0};
        gameLobbyList.forEach((a)->{
            if(!a.lobby.disabled()){
                mc[0]++;
            }
        });
        return mc[0];
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        if(gameLobbyList.size()==0){
            jsonObject.addProperty("successful",false);
            jsonObject.addProperty("message","no more game lobby");
            return jsonObject;
        }
        jsonObject.addProperty("successful",this.successful);
        jsonObject.addProperty("lobbySize",gameLobbyList.size());
        page = page<gameLobbyList.size()?page:0;
        GameLobby gameLobby = gameLobbyList.get(page);
        jsonObject.addProperty("index",page);
        jsonObject.add("lobby",gameLobby.toJson());
        return jsonObject;
    }
}
