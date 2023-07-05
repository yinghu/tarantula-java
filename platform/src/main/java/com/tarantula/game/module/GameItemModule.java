package com.tarantula.game.module;

import com.icodesoftware.*;


public class GameItemModule extends ModuleHeader{

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        throw new UnsupportedOperationException(session.action()+" not support");
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.context.log("Game item module started", OnLog.WARN);
    }

}
