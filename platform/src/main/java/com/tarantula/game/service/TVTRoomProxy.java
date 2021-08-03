package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.tarantula.game.Arena;

import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;


public class TVTRoomProxy extends RoomProxyHeader {

    @Override
    public GameRoom join(Session session, Arena arena, Rating rating) {
        return new GameRoom();
    }
    public void leave(String systemId){

    }
    public void onTimer(Module.OnUpdate onUpdate){

    }
}
