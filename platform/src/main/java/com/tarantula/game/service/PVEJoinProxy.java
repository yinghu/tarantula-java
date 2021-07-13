package com.tarantula.game.service;

import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;

public class PVEJoinProxy implements GameZone.JoinProxy {

    @Override
    public Stub join(Arena arena, Rating rating) {
        Stub stub = new Stub();
        stub.successful(true);
        stub.seat = rating.xpLevel;
        stub.rating = rating;
        stub.arena = arena.name();
        stub.owner(rating.distributionKey());
        return stub;
    }
}
