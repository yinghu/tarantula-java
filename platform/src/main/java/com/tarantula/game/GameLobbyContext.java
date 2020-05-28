package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class GameLobbyContext extends ResponseHeader {
    public List<GameLobby> gameLobbyList;

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",this.successful);
        JsonArray jds = new JsonArray();
        gameLobbyList.forEach((g)->{
            jds.add(g.toJson());
        });
        jsonObject.add("lobbyList",jds);
        return jsonObject;
    }
}
