package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Configurable;

import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.GameLobby;

public class DynamicGameLobbySetup extends DynamicLobbySetup{
    private static String CONFIG = "game-lobby-settings";
    @Override
    public void setup(ServiceContext serviceContext, Descriptor application, String configName) {
        GameLobby gameLobby = new GameLobby();
        gameLobby.distributionKey(application.distributionKey());
        Configuration configuration = serviceContext.configuration(CONFIG);
        super.setup(serviceContext,application,configName);
    }

    @Override
    public <T extends Configurable> T load(ApplicationContext context, Descriptor application) {
        return super.load(context,application);
    }

    @Override
    public <T extends Configurable> T load(ServiceContext context, Descriptor application) {
        return super.load(context,application);
    }
}
