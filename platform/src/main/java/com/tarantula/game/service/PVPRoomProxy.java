package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Rating;
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
        stub.stub(session.stub());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        GameRoom room = gameServiceProvider.roomServiceProvider().join(rating,gameZone);
        stub.joined(room!=null);
        if(!stub.joined()) return stub;
        stub.roomId = room.roomId();
        stub.zoneId = gameZone.distributionKey();
        stub.room = room;
        stub.zone = gameZone;
        stub.pushChannel = gameServiceProvider.roomServiceProvider().registerChannel(stub,(s,d)->{
            gameLobby.timeout(s,d);
        });
        stub.tag(application.tag());
        stub.ticket(this.context.validator().ticket(session.distributionId(),session.stub()));
        this.dataStore.update(stub);
        return stub;
    }
    public boolean leave(Stub stub){
        stub.joined(false);
        this.dataStore.update(stub);
        this.gameServiceProvider.roomServiceProvider().leave(stub);
        return true;
    }
}
