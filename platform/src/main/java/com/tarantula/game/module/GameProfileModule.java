package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.util.ListSerializer;
import com.tarantula.platform.presence.Profile;

import java.util.List;

public class GameProfileModule implements Module{
    private ApplicationContext context;
    private PlatformGameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onProfile")){
            Profile profile = gameServiceProvider.presenceServiceProvider().profile(session.systemId());
            session.write(profile.toJson().toString().getBytes());
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
        this.context.log("Game profile module started", OnLog.WARN);
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }
}
