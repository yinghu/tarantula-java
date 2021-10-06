package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.presence.RecentlyPlayList;

public class RecentlyPlayListModule implements Module , RecentlyPlayList.Listener {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.gameServiceProvider.presenceServiceProvider().registerListener(this.context.descriptor(),this);
        this.context.log("recently play list module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }

    @Override
    public void onPlay(String systemId, Descriptor lobby) {
        this.context.log(systemId+">>"+lobby.tag(),OnLog.WARN);
    }
}
