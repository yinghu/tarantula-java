package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.tarantula.game.*;
import com.tarantula.platform.room.GameRoom;

public class PVPRoomProxy extends RoomProxyHeader{


    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby, GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session, Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        GameRoom _rm = gameServiceProvider.roomServiceProvider().join(gameZone,rating);
        stub.room = _rm;
        stub.joined = _rm.roomId()!=null;
        stub.zone = gameZone;
        stub.rating = rating;
        stub.tag = application.tag();
        stub.serverKey = serverKey;
        this.dataStore.update(stub);
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        this.gameServiceProvider.roomServiceProvider().leave(stub.room.roomId(),stub.systemId());
    }
    @Override
    public void onTimer(Module.OnUpdate onUpdate) {
        //this.context.log("calling on ->"+registerKey,OnLog.WARN);
    }
}
