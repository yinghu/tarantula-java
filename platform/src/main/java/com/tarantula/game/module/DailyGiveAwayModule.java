package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;

public class DailyGiveAwayModule implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Daily giveaway module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }

}
