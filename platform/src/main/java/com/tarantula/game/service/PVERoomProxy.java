package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.*;

import java.util.concurrent.ConcurrentHashMap;

public class PVERoomProxy extends RoomProxyHeader {

    private ConcurrentHashMap<String,GameRoom> activeRoomIndex;

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
        activeRoomIndex = new ConcurrentHashMap<>();
    }
    @Override
    public Stub join(Session session,Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        GameRoom room = stub.room;
        if(room.distributionKey()!=null){
            this.dataStore.createIfAbsent(room,true);
        }
        else{
            this.dataStore.create(room);
        }
        room.arena = gameZone.arena(rating.arenaLevel);
        room.totalJoined = 1;
        if(application.tournamentEnabled()){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            room.instance = instance;
        }
        room.duration = room.arena.duration;
        room.round++;
        this.dataStore.update(room);
        stub.zone = gameZone;
        stub.arena = room.arena;
        stub.joined = true;
        stub.tag = application.tag();
        stub.tournamentEnabled = application.tournamentEnabled();
        stub.rating = rating;
        stub.statistics = gameServiceProvider.statistics(session.systemId());
        activeRoomIndex.put(room.distributionKey(),room);
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        activeRoomIndex.remove(stub.room.distributionKey());
        this.dataStore.update(stub.room);
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
    }
    @Override
    public void onTimer(Module.OnUpdate onUpdate) {
        //this.context.log("calling on ->"+registerKey,OnLog.WARN);
        activeRoomIndex.forEach((k,v)->{
            v.duration -= application.timerOnModule();
            if(v.duration>=0) dataStore.update(v);
            this.context.log(v.toString(),OnLog.WARN);
        });
    }
}
