package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.tarantula.game.*;

public class TVERoomProxy extends RoomProxyHeader {

    @Override
    public Stub join(Session session) {
        return new Stub();
    }
    public boolean leave(Stub stub){
        return false;
    }


}
