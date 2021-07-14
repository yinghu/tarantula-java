package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;

public class TVTRoomProxy implements GameZone.RoomProxy {

    @Override
    public Stub join(Arena arena, Rating rating) {
        return null;
    }
    public void leave(String systemId){

    }
    public void onTimer(Module.OnUpdate onUpdate){

    }
}
