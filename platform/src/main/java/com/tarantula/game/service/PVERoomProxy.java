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
        if(room.distributionKey()==null){//create new one
            this.dataStore.create(room);
            GameEntry gameEntry = new GameEntry(1);
            gameEntry.systemId = session.systemId();
            gameEntry.owner(room.distributionKey());
            dataStore.create(gameEntry);
            room.dataStore(dataStore);
            room.load();
        }
        else if(room.distributionKey()!=null&&(!activeRoomIndex.containsKey(room.distributionKey()))){
            this.dataStore.load(room);
            room.dataStore(dataStore);
            room.load();
        }
        else if(room.distributionKey()!=null&&activeRoomIndex.containsKey(room.distributionKey())){
            room = activeRoomIndex.get(room.distributionKey());
        }
        if(application.tournamentEnabled()&&session.tournamentId()!=null){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            room.setup(gameZone.arena(rating.arenaLevel),instance);
        }
        else{
            room.setup(gameZone.arena(rating.arenaLevel),null);
        }
        this.dataStore.update(room);
        stub.zone = gameZone;
        stub.joined = true;
        stub.serverKey = serverKey;
        stub.tag = application.tag();
        stub.rating = rating;
        stub.statistics = gameServiceProvider.statistics(session.systemId());
        stub.dailyLogin = gameServiceProvider.dailyLogin(session.systemId());
        activeRoomIndex.put(room.distributionKey(),room);
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        activeRoomIndex.remove(stub.room.distributionKey());
        stub.room.reset();
        this.dataStore.update(stub.room);
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
    }
    @Override
    public void onTimer(Module.OnUpdate onUpdate) {
        //this.context.log("calling on ->"+registerKey,OnLog.WARN);
        activeRoomIndex.forEach((k,v)->{
            //v.duration -= application.timerOnModule();
            //if(v.duration <=0){
                //gameLobby.timeout(v);
            //}
        });
    }
}
