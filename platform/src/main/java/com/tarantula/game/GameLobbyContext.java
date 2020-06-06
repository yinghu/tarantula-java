package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.concurrent.ConcurrentHashMap;

public class GameLobbyContext extends ResponseHeader {
    public ConcurrentHashMap<Integer,GameLobby> gameLobbyList;
    public int maxLobbyCount;
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
    public JsonObject availableSlots(){
        JsonObject jsonObject = new JsonObject();
        if(gameLobbyList.size()==maxLobbyCount){
            jsonObject.addProperty("successful",false);
            jsonObject.addProperty("message","max lobby count reached!");
            return jsonObject;
        }
        jsonObject.addProperty("successful",true);
        JsonArray jds = new JsonArray();
        for(int i=1;i<maxLobbyCount+1;i++){
            JsonObject st = new JsonObject();
            st.addProperty("slot",i);
            st.addProperty("added",gameLobbyList.containsKey(i));
            jds.add(st);
        }
        jsonObject.add("slots",jds);
        return jsonObject;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        GameLobby gameLobby = gameLobbyList.get(page);
        if(gameLobby==null){
            jsonObject.addProperty("successful",false);
            jsonObject.addProperty("message","lobby ["+page+"] is empty slot, you can add a lobby!");
            page = 0;
            return jsonObject;
        }
        jsonObject.addProperty("successful",this.successful);
        jsonObject.addProperty("message","lobby ["+page+"] loaded, you can edit it!");
        jsonObject.addProperty("lobbySize",gameLobbyList.size());
        jsonObject.addProperty("index",page);
        jsonObject.add("lobby",gameLobby.toJson());
        return jsonObject;
    }
}
