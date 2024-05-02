package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;


public class CloudDataModule extends ModuleHeader{

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        session.write(JsonUtil.toSimpleResponse(true,"item call").getBytes());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.context.log("Game cloud module started", OnLog.WARN);
    }

}
