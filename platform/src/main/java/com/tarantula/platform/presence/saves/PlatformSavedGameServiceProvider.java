package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;

import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.platform.item.PlatformItemServiceProvider;


public class PlatformSavedGameServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "save";

    private int mappingObjectMaxSize = 4000;

    public PlatformSavedGameServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject saveGame = ((JsonElement)configuration.property("savedGame")).getAsJsonObject();
        mappingObjectMaxSize = saveGame.get("mappingObjectMaxSize").getAsInt();
        this.logger = serviceContext.logger(PlatformSavedGameServiceProvider.class);
        this.logger.warn("Saved game service provider started on ->"+gameServiceName);
    }



    public int mappingObjectMaxSize(){
        return mappingObjectMaxSize;
    }

    public <T extends Recoverable> boolean save(T save){
        return this.dataStore.createIfAbsent(save,true);
    }

    public <T extends Recoverable> boolean load(T save){
        return this.dataStore.load(save);
    }

    public <T extends Recoverable> boolean reset(T save){
        return this.dataStore.update(save);
    }

    public CurrentSaveIndex selectSavedGame(Session session,SavedGame selected){
        CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex(session,selected.distributionKey());
        this.dataStore.createIfAbsent(currentSaveIndex,true);
        return currentSaveIndex;
    }


}
