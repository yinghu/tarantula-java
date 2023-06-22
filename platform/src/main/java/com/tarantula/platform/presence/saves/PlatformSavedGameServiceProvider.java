package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;

import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.platform.item.PlatformItemServiceProvider;

import java.time.LocalDateTime;


public class PlatformSavedGameServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "save";

    private int mappingObjectMaxSize = 4000;
    private int saveSize = 3;

    private long saveTimeout = 600000; //10 minutes

    public PlatformSavedGameServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject saveGame = ((JsonElement)configuration.property("savedGame")).getAsJsonObject();
        mappingObjectMaxSize = saveGame.get("mappingObjectMaxSize").getAsInt();
        saveSize = saveGame.get("saveSize").getAsInt();
        saveTimeout = saveGame.get("").getAsInt()*60*1000;
        this.logger = serviceContext.logger(PlatformSavedGameServiceProvider.class);
        this.logger.warn("Saved game service provider started on ->"+gameServiceName);
    }



    public int mappingObjectMaxSize(){
        return mappingObjectMaxSize;
    }
    public int saveSize(){
        return saveSize;
    }
    public <T extends Recoverable> void save(Session session,T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        PlayerSaveIndex saveIndex = playerSaveIndex(currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index());
        save.distributionKey(saveIndex.distributionKey());
        if(!this.dataStore.update(save)) {
            this.dataStore.createIfAbsent(save, false);
        }
        if(saveIndex.addKey(save.key().asString())) saveIndex.update();
        platformGameServiceProvider.presenceServiceProvider().updateSavedGame(currentSaveIndex);
    }

    public <T extends Recoverable> boolean load(Session session,T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        String saveId = currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index();
        save.distributionKey(saveId);
        return this.dataStore.load(save);
    }

    public CurrentSaveIndex reset(Session session){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        //reset or delete saved data associated with the save
        PlayerSaveIndex saveIndex = playerSaveIndex(currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index());
        saveIndex.keySet().forEach(k->{
            this.dataStore.delete(k.getBytes());
        });
        saveIndex.clear();
        return currentSaveIndex;
    }

    private CurrentSaveIndex currentSaveIndex(Session session){
        CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex(session);
        this.dataStore.createIfAbsent(currentSaveIndex,true);
        return currentSaveIndex;
    }

    public CurrentSaveIndex selectSavedGame(Session session,SavedGame selected,SavedGameSelected previousSelected){
        CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex(session,selected);
        this.dataStore.createIfAbsent(currentSaveIndex,true);
        if(currentSaveIndex.index()!=null && currentSaveIndex.index().equals(selected.distributionKey())) return currentSaveIndex;
        previousSelected.selected(currentSaveIndex);
        currentSaveIndex.index(selected.distributionKey());
        currentSaveIndex.name(selected.name());
        currentSaveIndex.version = selected.version;
        currentSaveIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        this.dataStore.update(currentSaveIndex);
        return currentSaveIndex;
    }
    private PlayerSaveIndex playerSaveIndex(String indexId){
        PlayerSaveIndex playerSaveIndex = new PlayerSaveIndex(indexId);
        this.dataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(this.dataStore);
        return playerSaveIndex;
    }


}
