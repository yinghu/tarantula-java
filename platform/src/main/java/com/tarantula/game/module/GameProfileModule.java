package com.tarantula.game.module;

import com.icodesoftware.*;
import com.tarantula.platform.presence.Profile;

public class GameProfileModule extends ModuleHeader{

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
        super.setup(applicationContext);
        this.context.log("Game profile module started", OnLog.WARN);
    }

}
