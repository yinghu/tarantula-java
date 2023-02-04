package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;


import com.icodesoftware.Session;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.service.Serviceable;


public interface GameLobby extends Configurable, Serviceable {


    Stub join(Session session, Rating rating);
    boolean leave(Session session);
    byte[] onService(Session session, byte[] payload);
    void validate(Session session);

    void setup(ApplicationContext applicationContext) throws Exception;
    boolean timeout(String systemId,int stub);

    GameServiceProxy gameServiceProxy(short serviceId);



}