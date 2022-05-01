package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;

public class InboxModule implements Module {
    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {

        throw new UnsupportedOperationException(session.action()+" not support");
        //return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Inbox module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }

}
