package com.tarantula.game.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameZone;
import com.tarantula.game.Stub;
import com.tarantula.platform.statistics.StatisticsSerializer;

abstract public class RoomProxyHeader implements GameZone.RoomProxy, GameLobby.TimerListener {

    protected ApplicationContext context;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;
    protected GameLobby gameLobby;
    protected GameZone gameZone;
    protected DataStore dataStore;
    protected String registerKey;

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        this.context = applicationContext;
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
        this.gameLobby = gameLobby;
        this.gameZone = gameZone;
        this.dataStore = gameZone.dataStore();
        this.registerKey = this.gameLobby.registerTimerListener(this);
    }
    @Override
    public void update(Session session, Stub stub, byte[] payload, Module.OnUpdate onUpdate){
        JsonObject jsonObject = JsonUtil.parse(payload);
        if(jsonObject.has("rating")){
            JsonObject delta = jsonObject.getAsJsonObject("rating");
            stub.rating.update(delta.get("rank").getAsInt(),delta.get("delta").getAsDouble(),stub.room.arena().xp).update();
            if(session.name().equals("rating")) session.write(stub.rating.toJson().toString().getBytes());
        }
        if(jsonObject.has("stats")){
            JsonArray stats = jsonObject.getAsJsonArray("stats");
            stats.forEach((a)->{
                JsonObject kv = a.getAsJsonObject();
                stub.statistics.entry(kv.get("name").getAsString()).update(kv.get("value").getAsDouble()).update();
            });
            if(session.name().equals("stats")){
                StatisticsSerializer serializer = new StatisticsSerializer();
                session.write(serializer.serialize(stub.statistics,Statistics.class,null).toString().getBytes());
            }
        }
        if(application.tournamentEnabled()&&jsonObject.has("tournament")){
            JsonObject score = jsonObject.getAsJsonObject("tournament");
            Tournament.Entry entry = gameServiceProvider.tournamentServiceProvider().score(stub.room.tournament().distributionKey(),session.systemId(),score.get("score").getAsDouble());
            if(session.name().equals("tournament")){
                session.write(entry.toJson().toString().getBytes());
            }
        }
    }
    public void close(){
        gameServiceProvider.releaseRoomProxy(registerKey);
    }
}
