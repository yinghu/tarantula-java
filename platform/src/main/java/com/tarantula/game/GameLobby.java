package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;

import com.icodesoftware.Initializer;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.service.Serviceable;


public interface GameLobby extends Configurable, Serviceable {


    Stub join(Session session, Rating rating);
    boolean leave(Session session);
    byte[] onService(Session session, byte[] payload);
    void validate(Session session);

    void setup(ApplicationContext applicationContext) throws Exception;
    boolean timeout(String systemId,int stub);

    ServiceProxy serviceProxy(short serviceId);

    interface ServiceProxy extends Initializer {

        short serviceId();

        boolean exported();

        //from http endpoint
        byte[] onService(Session session,byte[] payload);

        //from udp endpoint
        byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer);
    }

}