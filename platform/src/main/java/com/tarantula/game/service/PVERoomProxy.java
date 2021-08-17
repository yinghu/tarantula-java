package com.tarantula.game.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;


public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session,String zoneId, Rating rating) {
        GameRoom room = new GameRoom(true);
        if(application.tournamentEnabled()){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            room.instance = instance;
        }
        room.arena = gameZone.arena(rating.arenaLevel);
        return room;
    }
    @Override
    public void update(Session session, Stub stub,byte[] payload){
        Rating rating = stub.rating;
        JsonObject jsonObject = JsonUtil.parse(payload);
        rating.update(jsonObject.get("rank").getAsInt(),jsonObject.get("delta").getAsDouble(),stub.arena.xp);
        rating.update();
        session.write(rating.toJson().toString().getBytes());
        JsonArray stats = jsonObject.getAsJsonArray("stats");
        Statistics statistics = this.gameServiceProvider.statistics(session.systemId());
        stats.forEach((a)->{
            JsonObject kv = a.getAsJsonObject();
            statistics.entry(kv.get("name").getAsString()).update(kv.get("value").getAsDouble()).update();
        });
    }
    public void leave(Stub stub){
        this.context.log(stub.systemId()+" leave room", OnLog.WARN);
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
    }
    public void onTimer(Module.OnUpdate onUpdate){
        //this.context.log("ON TIMER->"+gameZone.distributionKey(), OnLog.WARN);
    }
}
