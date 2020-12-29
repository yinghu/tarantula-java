package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;

/**
 * Created by yinghu lu on 12/28/2020.
 */
public class ItemModule implements Module {
    private ApplicationContext context;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("started item module", OnLog.WARN);
    }

    @Override
    public String label() {
        return "item";
    }
}
