package com.tarantula.game.service;

import com.tarantula.game.*;

public class PVERoomProxy extends RoomProxyHeader {


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
