package com.tarantula.game.service;

import com.icodesoftware.*;
import com.tarantula.game.*;
import com.tarantula.platform.messaging.PlatformMessagingServiceProvider;
import com.tarantula.platform.room.GameRoom;


public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session,Rating rating) {
        Stub stub = gameServiceProvider.presenceServiceProvider().stub(session,application);
        GameRoom room = this.gameServiceProvider.roomServiceProvider().join(rating,gameZone);
        stub.joined = room!=null;
        if(!stub.joined) return stub;
        stub.room = room;
        stub.roomId = stub.room.roomId();
        stub.zone = gameZone;
        stub.zoneId = gameZone.distributionKey();
        stub.pushChannel = this.gameServiceProvider.roomServiceProvider().registerChannel(stub,(s,d)->{
            gameLobby.timeout(s,d);
        });
        if(application.tournamentEnabled() && session.tournamentId()!=null){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().enter(session.tournamentId(),session.systemId());
            stub.tournamentId(session.tournamentId());
            stub.trackId(instance.distributionKey());
            stub.tournament = instance;
        }
        stub.offline = true;
        stub.tag(application.tag());
        stub.ticket(this.context.validator().ticket(session.distributionId(),session.stub()));
        stub.update();
        return stub;
    }
    public boolean leave(Stub stub){
        stub.joined = false;
        stub.update();
        this.gameServiceProvider.roomServiceProvider().leave(stub);
        if(application.tournamentEnabled()&&stub.tournament!=null){
            gameServiceProvider.tournamentServiceProvider().finish(stub.tournamentId(),stub.trackId(),stub.systemId());
        }
        return true;
    }
}
