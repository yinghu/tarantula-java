package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.tarantula.game.*;

public class PVPRoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session, Arena arena, Rating rating) {
        GameRoom room = gameServiceProvider.roomServiceProvider().join(arena,rating);
        Stub stub = new Stub();
        stub.successful(true);
        stub.roomId = room.roomId;
        stub.rating = rating;
        stub.arena = arena;
        stub.owner(rating.distributionKey());
        return new GameRoom(true);
    }
    public void leave(String systemId){

    }
    public void onTimer(Module.OnUpdate onUpdate){
        //gameServiceProvider.roomServiceProvider().leave(arena,_remote.roomId,rating.distributionKey());
    }
}
