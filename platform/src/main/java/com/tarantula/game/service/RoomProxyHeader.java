package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.tarantula.game.GameZone;

abstract public class RoomProxyHeader implements GameZone.RoomProxy {

    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;

    @Override
    public void setup(ApplicationContext applicationContext) {
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
    }
}
