package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.*;

public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session,Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);

        GameRoom room = new GameRoom(true);
        room.arena = gameZone.arena(rating.arenaLevel);
        room.totalJoined = 1;
        room.distributionKey(gameZone.distributionKey());
        if(application.tournamentEnabled()){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            room.instance = instance;
        }
        stub.room = room;
        stub.zone = gameZone;
        stub.arena = room.arena;
        stub.joined = true;
        stub.tag = application.tag();
        stub.rating = rating;
        stub.statistics = gameServiceProvider.statistics(session.systemId());
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
    }
    @Override
    public void onTimer(Module.OnUpdate onUpdate) {
        //this.context.log("calling on ->"+registerKey,OnLog.WARN);
    }
}
