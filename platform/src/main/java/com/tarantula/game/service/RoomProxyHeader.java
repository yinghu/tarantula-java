package com.tarantula.game.service;


import com.icodesoftware.*;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameZone;

abstract public class RoomProxyHeader implements GameZone.RoomProxy {

    protected ApplicationContext context;
    protected PlatformGameServiceProvider gameServiceProvider;
    protected Descriptor application;
    protected GameLobby gameLobby;
    protected GameZone gameZone;
    protected DataStore dataStore;

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        this.context = applicationContext;
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
        this.gameLobby = gameLobby;
        this.gameZone = gameZone;
        this.dataStore = gameZone.dataStore();
    }

}
