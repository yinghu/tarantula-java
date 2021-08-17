package com.tarantula.game.service;

import com.icodesoftware.*;
import com.tarantula.game.*;

public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session,String zoneId, Rating rating) {
        GameRoom room = new GameRoom(true);
        if(application.tournamentEnabled()){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            room.instance = instance;
        }
        room.arena = gameZone.arena(rating.arenaLevel);
        room.totalJoined = 1;
        room.distributionKey(zoneId);
        return room;
    }
    public void leave(Stub stub){
        this.context.log(stub.systemId()+" leave room", OnLog.WARN);
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
    }
}
