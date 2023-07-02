package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.inbox.Inbox;


public class GameInboxModule implements Module{
    private ApplicationContext context;
    private PlatformGameServiceProvider gameServiceProvider;

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
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        if(this.descriptor().accessMode() == Access.PRIVATE_ACCESS_MODE) this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Game inbox module started -"+this.context.descriptor().tag(), OnLog.WARN);
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }
}
