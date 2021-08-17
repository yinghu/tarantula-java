package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.tarantula.game.GameZone;
import com.tarantula.game.Stub;

abstract public class RoomProxyHeader implements GameZone.RoomProxy {

    protected ApplicationContext context;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;
    protected GameZone gameZone;

    @Override
    public void setup(ApplicationContext applicationContext,GameZone gameZone) {
        this.context = applicationContext;
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
        this.gameZone = gameZone;
    }
    @Override
    public void update(Session session, Stub stub,byte[] payload){
    }
}
