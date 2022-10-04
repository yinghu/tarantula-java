package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.service.Serviceable;


public interface GameLobby extends Configurable, Serviceable {


    Stub join(Session session, Rating rating);
    void leave(Session session);
    void update(Session session, byte[] payload);
    void list(Session session);
    void validate(Session session);

    void setup(ApplicationContext applicationContext) throws Exception;
    boolean timeout(String systemId,int stub);

    interface ServiceMessageListener{

        byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer);
        void setup(ApplicationContext applicationContext);
    }

}