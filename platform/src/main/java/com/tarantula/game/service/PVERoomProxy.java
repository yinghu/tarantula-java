package com.tarantula.game.service;

import com.icodesoftware.*;
import com.tarantula.game.*;
import com.tarantula.platform.room.GameRoom;


public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session,Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.stub(session.stub());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        GameRoom room = this.gameServiceProvider.roomServiceProvider().join(rating,gameZone);
        stub.joined = room!=null;
        if(!stub.joined) return stub;
        stub.room = room;
        stub.roomId = stub.room.roomId();
        stub.zone = gameZone;
        stub.zoneId = gameZone.distributionKey();
        stub.pushChannel = this.gameServiceProvider.roomServiceProvider().registerChannel(stub,(h,m)->super.onService(stub,h,m),(s,d)->{
            gameLobby.timeout(s,d);
        });
        stub.joined = stub.pushChannel!=null;
        if(!stub.joined) {
            leave(stub);
            return stub;
        }
        if(application.tournamentEnabled()&&session.tournamentId()!=null){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().enter(session.tournamentId(),session.systemId());
            stub.tournament = instance;
        }
        stub.offline = true;
        stub.tag(application.tag());
        stub.ticket(this.context.validator().ticket(session.systemId(),session.stub()));
        stub.rating = rating;
        this.dataStore.update(stub);
        return stub;
    }
    public boolean leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        this.gameServiceProvider.roomServiceProvider().leave(stub);
        if(application.tournamentEnabled()&&stub.tournament!=null){
            gameServiceProvider.tournamentServiceProvider().finish("",stub.tournament.distributionKey(),stub.systemId());
        }
        return true;
    }
}
