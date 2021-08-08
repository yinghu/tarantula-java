package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.tarantula.game.*;

public class PVPRoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session, Arena arena, Rating rating) {
        arena.owner(gameZone.distributionKey());
        GameRoom room = gameServiceProvider.roomServiceProvider().join(arena,rating);
        return room;
    }
    public void leave(String systemId){

    }
    public void onTimer(Module.OnUpdate onUpdate){
        //gameServiceProvider.roomServiceProvider().leave(arena,_remote.roomId,rating.distributionKey());
    }
}
