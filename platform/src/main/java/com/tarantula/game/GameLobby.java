package com.tarantula.game;

import com.icodesoftware.Configurable;
import com.icodesoftware.Initializer;
import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.icodesoftware.service.Serviceable;

import java.util.List;

public interface GameLobby extends Configurable, Initializer, Serviceable {

    List<GameZone> list();
    Stub join(Session session, Rating rating);
    void leave(String systemId);
    void update(String systemId);
    void onTimer(Module.OnUpdate onUpdate);

}