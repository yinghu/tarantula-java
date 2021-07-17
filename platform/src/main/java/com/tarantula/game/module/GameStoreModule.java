package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;

public class GameStoreModule implements Module {
    private ApplicationContext context;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("game store module started", OnLog.WARN);
    }
}
