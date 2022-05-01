package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;

import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.presence.saves.PlayerSavedGames;

public class SavedGameModule implements Module {
    private ApplicationContext context;
    private PlatformPresenceServiceProvider presenceServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")) {
            PlayerSavedGames playerSavedGames = new PlayerSavedGames(this.presenceServiceProvider.listSaves(session.systemId(),session.name()));
            session.write(playerSavedGames.toJson().toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        GameServiceProvider gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.presenceServiceProvider = gameServiceProvider.presenceServiceProvider();
        //this.achievementServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Saved game module started", OnLog.WARN);
    }
}
