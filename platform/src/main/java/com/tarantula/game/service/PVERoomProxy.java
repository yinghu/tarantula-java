package com.tarantula.game.service;

import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;

public class PVERoomProxy implements GameZone.RoomProxy {

    @Override
    public Stub join(Arena arena, Rating rating) {
        Stub stub = new Stub();
        stub.successful(true);
        stub.rating = rating;
        stub.arena = arena;
        stub.offline = true;
        stub.owner(rating.distributionKey());
        return stub;
    }
    public void leave(String systemId){

    }
}
