package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.tarantula.game.*;

public class TVERoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session, String zoneId, Rating rating) {
        return new GameRoom();
    }
    public void leave(Stub stub){

    }
    public void onTimer(Module.OnUpdate onUpdate){

    }

}
