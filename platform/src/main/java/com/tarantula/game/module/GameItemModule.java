package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;


public class GameItemModule extends ModuleHeader{

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        session.write(JsonUtil.toSimpleResponse(true,"item call").getBytes());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.context.log("Game item module started", OnLog.WARN);
    }

}
