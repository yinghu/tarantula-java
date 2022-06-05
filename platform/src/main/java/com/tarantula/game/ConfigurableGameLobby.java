package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.lobby.LobbyItem;


public class ConfigurableGameLobby extends RecoverableObject implements GameLobby{

    private LobbyItem lobbyItem;

    public ConfigurableGameLobby(LobbyItem lobbyItem){
        this.lobbyItem = lobbyItem;
    }



    @Override
    public Stub join(Session session, Rating rating) {
        return null;
    }

    @Override
    public void leave(Session session) {

    }

    @Override
    public void update(Session session, byte[] payload) {

    }

    @Override
    public void list(Session session) {

    }




    @Override
    public boolean timeout(String systemId) {
        return false;
    }

    @Override
    public ServiceMessageListener ServiceMessageListener(short serviceCommand) {
        return null;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {

    }


    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
