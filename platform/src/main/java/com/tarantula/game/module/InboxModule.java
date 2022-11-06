package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.inbox.Inbox;

public class InboxModule implements Module {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onInbox")){
            Inbox inbox = gameServiceProvider.inboxServiceProvider().inbox(session.systemId());
            session.write(inbox.toJson().toString().getBytes());
        }
        else {
            throw new UnsupportedOperationException(session.action() + " not support");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.context.log("["+context.descriptor().typeId()+ "] inbox module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }
}
