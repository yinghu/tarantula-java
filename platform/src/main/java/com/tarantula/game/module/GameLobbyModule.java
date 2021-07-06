package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;

public class GameLobbyModule implements Module, Configurable.Listener, Connection.OnConnectionListener {

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    @Override
    public void onJoin(Session session, Module.OnUpdate onUpdate) throws Exception{

    }

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = applicationContext.serviceProvider(context.descriptor().typeId().replace("lobby","service"));
        //this.gameServiceProvider.registerConfigurableListener()
        this.context.log("Game lobby started on tag ["+context.descriptor().tag()+"]",OnLog.WARN);
    }

    //configurable listener
    @Override
    public <T extends Configurable> void onUpdated(T updated){

    }

    @Override
    public <T extends Configurable> void onCreated(T updated){

    }

    //connection listener
    @Override
    public String lobbyTag() {
        return this.context.descriptor().tag();
    }

    @Override
    public void onConnection(Session session) {

    }
}
