package com.tarantula.game.service;


import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameZone;
import com.tarantula.game.Stub;
import com.tarantula.platform.achievement.AchievementProgress;


abstract public class RoomProxyHeader implements GameZone.RoomProxy {

    protected ApplicationContext context;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;
    protected GameLobby gameLobby;
    protected GameZone gameZone;
    protected DataStore dataStore;

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        this.context = applicationContext;
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
        this.gameLobby = gameLobby;
        this.gameZone = gameZone;
        this.dataStore = gameZone.dataStore();
    }
    @Override
    public void update(Session session, Stub stub, byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        boolean response = false;
        if(jsonObject.has("rating")){
            JsonObject delta = jsonObject.getAsJsonObject("rating");
            stub.rating.update(delta.get("rank").getAsInt(),delta.get("delta").getAsDouble(),stub.room.arena().xp).update();
            if(session.name().equals("rating")) {
                session.write(stub.rating.toJson().toString().getBytes());
                response = true;
            }
        }
        if(jsonObject.has("achievement")){
            JsonObject delta = jsonObject.getAsJsonObject("achievement");
            AchievementProgress progress = this.gameServiceProvider.achievementServiceProvider().onProgress(session.systemId(),session.name(),delta.get("progress").getAsDouble());
            if(session.name().equals("achievement")){
                session.write(progress.toJson().toString().getBytes());
                response = true;
            }
        }
        if(application.tournamentEnabled()&&jsonObject.has("tournament")){
            JsonObject score = jsonObject.getAsJsonObject("tournament");
            if(stub.tournament!=null){
                Tournament.Entry entry = gameServiceProvider.tournamentServiceProvider().score(stub.tournament.distributionKey(),session.systemId(),score.get("score").getAsDouble());
                if(session.name().equals("tournament")){
                    session.write(entry.toJson().toString().getBytes());
                    response = true;
                }
            }
            else if(stub.tournament==null&&session.name().equals("tournament")){
                session.write(JsonUtil.toSimpleResponse(false,"no tournament joined").getBytes());
                response = true;
            }
        }
        if(!response){
            session.write(JsonUtil.toSimpleResponse(false,"no response header setup").getBytes());
        }
    }
    public void list(Session session,Stub stub){
        if(session.name().equals("tournament")&&stub.tournament!=null){
            Tournament.RaceBoard board = gameServiceProvider.tournamentServiceProvider().list(stub.tournament.distributionKey());
            session.write(board.toJson().toString().getBytes());
        }
        else{
            session.write(JsonUtil.toSimpleResponse(false,"no tournament joined").getBytes());
        }
    }
    public void close(){

    }
    public byte[] update(Stub stub,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer){
        short cmd = messageBuffer.readShort();
        GameLobby.ServiceMessageListener messageListener = ServiceCommand.messageListener(cmd);
        messageListener.setup(this.context);
        return messageListener.update(stub,messageHeader,messageBuffer);
    }

}
