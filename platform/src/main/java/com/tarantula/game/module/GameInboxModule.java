package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.inbox.Inbox;


public class GameInboxModule extends ModuleHeader{

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onInbox")){
            Inbox inbox = this.gameServiceProvider.inboxServiceProvider().inbox(session.systemId());
            session.write(inbox.toJson().toString().getBytes());
        }
        else if(session.action().equals("onRedeem")){
            boolean suc = this.gameServiceProvider.inboxServiceProvider().redeem(session,session.name());
            session.write(JsonUtil.toSimpleResponse(suc,session.name()).getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.context.log("Game inbox module started -"+this.context.descriptor().tag(), OnLog.WARN);
    }

}
