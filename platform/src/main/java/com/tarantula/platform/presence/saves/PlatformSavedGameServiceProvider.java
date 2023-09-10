package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;

import com.icodesoftware.Session;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;

import java.time.LocalDateTime;
import java.util.List;


public class PlatformSavedGameServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "save";

    private int mappingObjectMaxSize = 4000;
    private int saveSize = 3;

    private long saveTimeout = 1; //1 hour


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
        saveTimeout = saveGame.get("saveTimeout").getAsInt()*saveTimeout;
        dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformSavedGameServiceProvider.class);
        this.logger.warn("Saved game service provider started on ->"+gameServiceName);
    }



    public int mappingObjectMaxSize(){
        return mappingObjectMaxSize;
    }

    public <T extends RecoverableObject> void save(Session session,T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        PlayerSaveIndex saveIndex = playerSaveIndex(currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index());
        save.distributionKey(saveIndex.distributionKey());
        if(!this.dataStore.update(save)) {
            this.dataStore.createIfAbsent(save, false);
        }
        save.dataStore(dataStore);
        if(saveIndex.addKey(save.key().asString())) saveIndex.update();
        platformGameServiceProvider.presenceServiceProvider().updateSavedGame(currentSaveIndex);
    }

    public <T extends RecoverableObject> void createIfAbsent(Session session, T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        String saveId = currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index();
        save.distributionKey(saveId);
        save.dataStore(dataStore);
        if(!this.dataStore.createIfAbsent(save,true)) return;
        PlayerSaveIndex saveIndex = playerSaveIndex(saveId);
        if(saveIndex.addKey(save.key().asString())) saveIndex.update();
        platformGameServiceProvider.presenceServiceProvider().updateSavedGame(currentSaveIndex);
    }

    public <T extends RecoverableObject> boolean load(Session session, T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        String saveId = currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index();
        save.distributionKey(saveId);
        save.dataStore(dataStore);
        return this.dataStore.load(save);
    }

    public CurrentSaveIndex reset(Session session){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        //reset or delete saved data associated with the save
        PlayerSaveIndex saveIndex = playerSaveIndex(currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index());
        saveIndex.keySet().forEach(k->{
            //this.dataStore.delete(k);
        });
        saveIndex.clear();
        return currentSaveIndex;
    }

    private CurrentSaveIndex currentSaveIndex(Session session){
        CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex(session);
        PlayerSessionIndex playerSessionIndex = playerSessionIndex(session.systemId());
        if(playerSessionIndex.load(currentSaveIndex)) return currentSaveIndex;
        currentSaveIndex.version = 1;
        currentSaveIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        playerSessionIndex.update(currentSaveIndex);
        return currentSaveIndex;
    }

    public CurrentSaveIndex selectSavedGame(Session session,SavedGame selected,SavedGameSelected previousSelected){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        if(currentSaveIndex.index()!=null && currentSaveIndex.index().equals(selected.distributionKey())) return currentSaveIndex;
        previousSelected.selected(currentSaveIndex);
        currentSaveIndex.index(selected.distributionKey());
        currentSaveIndex.name(selected.name());
        currentSaveIndex.version = selected.version;
        currentSaveIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        playerSessionIndex(session.systemId()).update(currentSaveIndex);
        return currentSaveIndex;
    }

    public void checkSavedGame(String systemId){
        PlayerSessionIndex playerSessionIndex = playerSessionIndex(systemId);
        playerSessionIndex.check(saveTimeout,selected-> platformGameServiceProvider.presenceServiceProvider().expireSavedGame(selected));
        //free previous save selection
    }
    private PlayerSaveIndex playerSaveIndex(String indexId){
        PlayerSaveIndex playerSaveIndex = new PlayerSaveIndex(indexId);
        this.dataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(this.dataStore);
        return playerSaveIndex;
    }
    private PlayerSessionIndex playerSessionIndex(String systemId){
        PlayerSessionIndex playerSessionIndex = new PlayerSessionIndex(systemId);
        this.dataStore.createIfAbsent(playerSessionIndex,true);
        playerSessionIndex.dataStore(dataStore);
        return playerSessionIndex;
    }

    public List<SavedGame> savedGameList(Session session){
        //PresencePortableRegistry registry = PresencePortableRegistry.INS;
        RecoverableQuery<SavedGame> query = new RecoverableQuery<>(session.key(),SavedGame.USER_SAVE,PresencePortableRegistry.SAVED_GAME_CID,PresencePortableRegistry.INS);
        List<SavedGame> list = dataStore.list(query);//dataStore.list(new SavedGameQuery<>(session.key()));
        if(list.size()==0){
            for(int i=0;i<saveSize;i++){
                SavedGame save = new SavedGame();
                save.name("save"+i);
                save.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                save.ownerKey(session.key());
                dataStore.create(save);
                list.add(save);
            }
        }
        return list;
    }

}
