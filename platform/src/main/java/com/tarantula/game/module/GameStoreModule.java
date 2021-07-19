package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;

public class GameStoreModule implements Module,Configurable.Listener{
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.registerConfigurableListener(this.context.descriptor().category(),this);
        this.context.log("game store module started", OnLog.WARN);
    }
    public <T extends Configurable> void onCreated(T created){
        this.context.log(created.configurationType(),OnLog.WARN);
    }

}
