package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.tarantula.game.*;

public class PVPRoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session, String zoneId, Rating rating) {
        GameRoom room = gameServiceProvider.roomServiceProvider().join(zoneId,rating);
        return room;
    }
    public void leave(Stub stub){
        this.gameServiceProvider.roomServiceProvider().leave(stub.arena,stub.roomId,stub.owner());
    }
    public void onTimer(Module.OnUpdate onUpdate){
        //gameServiceProvider.roomServiceProvider().leave(arena,_remote.roomId,rating.distributionKey());
    }
}
