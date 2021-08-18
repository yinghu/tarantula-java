package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.tarantula.game.*;

public class PVPRoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session, Rating rating) {
        //GameRoom room = gameServiceProvider.roomServiceProvider().join(zoneId,rating);
        return new GameRoom();
    }
    public void leave(Stub stub){
        //this.gameServiceProvider.roomServiceProvider().leave(stub.arena,stub.room.roomId(),stub.owner());
    }

}
