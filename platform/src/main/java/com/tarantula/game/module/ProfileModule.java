package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.presence.PresenceServiceProvider;
import com.tarantula.platform.presence.Profile;

public class ProfileModule implements Module  {

    private ApplicationContext context;

    private PresenceServiceProvider presenceServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onLoad")){
            session.write(this.presenceServiceProvider.profile(session.systemId()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdate")){
            Profile profile = this.presenceServiceProvider.profile(session.systemId());
            profile.configureAndValidate(payload);
            session.write(profile.toJson().toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        GameServiceProvider gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.presenceServiceProvider = gameServiceProvider.presenceServiceProvider();
        this.context.log("Profile module started", OnLog.WARN);
    }
}
