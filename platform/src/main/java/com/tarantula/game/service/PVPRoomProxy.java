package com.tarantula.game.service;

import com.icodesoftware.Module;
import com.tarantula.game.Arena;
import com.tarantula.game.Rating;
import com.tarantula.game.Room;
import com.tarantula.game.Stub;

public class PVPRoomProxy extends RoomProxyHeader {

    @Override
    public Stub join(Arena arena, Rating rating) {
        Room room = gameServiceProvider.roomServiceProvider().join(arena,rating);
        Stub stub = new Stub();
        stub.successful(true);
        stub.roomId = room.roomId;
        stub.rating = rating;
        stub.arena = arena;
        stub.owner(rating.distributionKey());
        return stub;
    }
    public void leave(String systemId){

    }
    public void onTimer(Module.OnUpdate onUpdate){
        //gameServiceProvider.roomServiceProvider().leave(arena,_remote.roomId,rating.distributionKey());
    }
}
