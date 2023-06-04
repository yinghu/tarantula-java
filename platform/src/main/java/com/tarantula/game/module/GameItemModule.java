package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;

public class GameItemModule implements Module{
    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        throw new UnsupportedOperationException(session.action()+" not support");
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Game item module started", OnLog.WARN);
    }

}
