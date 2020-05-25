package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.Access;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.GameCluster;

import java.util.List;

public class AdminContext extends ResponseHeader {
    public List<GameCluster> gameClusterList;
    public List<Access> userList;

    public AdminContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        if(gameClusterList!=null){
            JsonArray glist = new JsonArray();
            gameClusterList.forEach((g)->{
                JsonObject jo = new JsonObject();
                jo.addProperty("gameClusterId",g.distributionKey());
                jo.addProperty("name",(String)g.property(GameCluster.NAME));
                jo.addProperty("plan",(String)g.property(GameCluster.PLAN));
                jo.addProperty("gameLobby",(String)g.property(GameCluster.GAME_LOBBY));
                jo.addProperty("gameService",(String)g.property(GameCluster.GAME_SERVICE));
                jo.addProperty("gameData",(String)g.property(GameCluster.GAME_DATA));
                jo.addProperty("disabled",(boolean)g.property(GameCluster.DISABLED));
                glist.add(jo);
            });
            jsonObject.add("gameClusterList",glist);
        }
        return jsonObject;
    }

}
