package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.Access;
import com.tarantula.Lobby;
import com.tarantula.Statistics;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.GameCluster;
import com.tarantula.platform.service.Metrics;

import java.util.List;

public class AdminContext extends ResponseHeader {
    public List<GameCluster> gameClusterList;
    public List<Access> userList;
    public Metrics metrics;
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
                jo.addProperty("accessKey",(String)g.property(GameCluster.ACCESS_KEY));
                jo.addProperty("disabled",(Boolean)g.property(GameCluster.DISABLED));
                glist.add(jo);
            });
            jsonObject.add("gameClusterList",glist);
        }
        if(metrics!=null){
            JsonArray ja = new JsonArray();
            for(Statistics.Entry kv : metrics.statistics.summary()){
                JsonObject xv = new JsonObject();
                xv.addProperty("name",Metrics.toName(kv.name()));
                xv.addProperty("daily",kv.daily());
                xv.addProperty("weekly",kv.weekly());
                xv.addProperty("monthly",kv.monthly());
                xv.addProperty("yearly",kv.yearly());
                xv.addProperty("total",kv.total());
                ja.add(xv);
            }
            jsonObject.add("stats",ja);
        }
        return jsonObject;
    }


}
