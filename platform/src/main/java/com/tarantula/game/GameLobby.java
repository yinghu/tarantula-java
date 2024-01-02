package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;

import com.icodesoftware.Session;
import com.icodesoftware.service.Serviceable;


public interface GameLobby extends Configurable, Serviceable {


    Stub join(Session session);
    boolean leave(Session session);

    void validate(Session session);

    void setup(ApplicationContext applicationContext) throws Exception;
    boolean timeout(String systemId,long stub);

    //String gameModule();

}