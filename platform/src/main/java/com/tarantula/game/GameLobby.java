package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;
import com.icodesoftware.Initializer;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.service.Serviceable;

import java.util.List;

public interface GameLobby extends Configurable, Initializer, Serviceable {

    List<GameZone> list();
    Stub join(Session session, Rating rating);
    void leave(Session session);
    void update(Session session, byte[] payload);
    void list(Session session);

    boolean configureGameZone(byte[] payload);
    boolean configureArena(byte[] payload);
    void reload();

    boolean timeout(String systemId);

    ServiceMessageListener ServiceMessageListener(short serviceCommand);

    interface ServiceMessageListener{

        byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer);
        void setup(ApplicationContext applicationContext);
    }

}