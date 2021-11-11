package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
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
        GameRoom _joined = gameServiceProvider.roomServiceProvider().join(gameZone,rating);
        if(_joined==null) {
            stub.joined = false;
            return stub;
        }
        stub.room = _joined;
        stub.joined = true;
        stub.zone = gameZone;
        stub.rating = rating;
        stub.pushChannel = context.register(session.systemId(),(h,m)->super.update(stub,h,m));
        stub.tag = application.tag();
        stub.ticket = this.context.validator().ticket(session.systemId(),session.stub());
        this.dataStore.update(stub);
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        this.gameServiceProvider.roomServiceProvider().leave(stub.room.roomId(),stub.systemId());
    }
}
