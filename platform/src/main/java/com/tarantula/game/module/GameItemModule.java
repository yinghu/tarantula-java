package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.PlatformGameServiceProvider;

public class GameItemModule implements Module{
    private ApplicationContext context;
    private PlatformGameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        throw new UnsupportedOperationException(session.action()+" not support");
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        if(this.descriptor().accessMode() == Access.PRIVATE_ACCESS_MODE) this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Game item module started", OnLog.WARN);
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }
}
