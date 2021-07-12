package com.tarantula.game.service;

import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;

public class PVEJoinProxy implements GameZone.JoinProxy {

    @Override
    public Stub join(Rating rating) {
        Stub stub = new Stub();
        stub.successful(true);
        stub.seat = rating.xpLevel;
        stub.owner(rating.distributionKey());
        return stub;
    }
}
