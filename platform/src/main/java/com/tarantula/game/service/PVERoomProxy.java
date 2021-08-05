package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.tarantula.game.*;

public class PVERoomProxy extends RoomProxyHeader {


    @Override
    public GameRoom join(Session session,Arena arena, Rating rating) {
        if(application.tournamentEnabled()){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            GameRoom room = new GameRoom(true);
            room.instance = instance;
            return room;
        }
        return new GameRoom(true);
    }
    public void leave(String systemId){
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
    }
}
