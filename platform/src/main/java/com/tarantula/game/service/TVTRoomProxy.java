package com.tarantula.game.service;

import com.icodesoftware.Session;

import com.tarantula.game.Rating;
import com.tarantula.game.Stub;


public class TVTRoomProxy extends RoomProxyHeader {

    @Override
    public Stub join(Session session, Rating rating) {
        return new Stub();
    }
    public boolean leave(Stub stub){
        return false;
    }

    public void onTimer(){

    }
}
