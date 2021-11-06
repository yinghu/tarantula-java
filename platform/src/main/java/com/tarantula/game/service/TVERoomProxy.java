package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.tarantula.game.*;

public class TVERoomProxy extends RoomProxyHeader {

    @Override
    public Stub join(Session session, Rating rating) {
        return new Stub();
    }
    public void leave(Stub stub){

    }

    public void onTimer(){

    }

}
