package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class GameClusterContext extends ResponseHeader {
    public List<GameCluster> gameClusterList;
    public int index;
    public int pageSize=1;
    public GameClusterContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        JsonArray glist = new JsonArray();
        if(index>=gameClusterList.size()) index=0;
        int len = pageSize;
        for(int i=index;i<gameClusterList.size();i++){
            GameCluster g = gameClusterList.get(i);
            len--;
            JsonObject jo = new JsonObject();
            jo.addProperty("gameClusterId",g.distributionKey());
            jo.addProperty("name",(String)g.property(GameCluster.NAME));
            jo.addProperty("mode",(String)g.property(GameCluster.MODE));
            jo.addProperty("setup",(String)g.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            jo.addProperty("gameLobby",(String)g.property(GameCluster.GAME_LOBBY));
            jo.addProperty("gameService",(String)g.property(GameCluster.GAME_SERVICE));
            jo.addProperty("gameData",(String)g.property(GameCluster.GAME_DATA));
            jo.addProperty("accessKey",(String)g.property(GameCluster.ACCESS_KEY));
            jo.addProperty("tournamentEnabled",(Boolean)g.property(GameCluster.TOURNAMENT_ENABLED));
            jo.addProperty("disabled",(Boolean)g.property(GameCluster.DISABLED));
            glist.add(jo);
            if(len==0) break;

        }
        jsonObject.addProperty("index",index+(pageSize-len));
        jsonObject.add("gameClusterList",glist);
        return jsonObject;
    }


}
