package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;

import com.icodesoftware.Session;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.GamePortableRegistry;
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

    public CurrentSaveIndex currentSaveIndex(Session session){
        CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex();
        RecoverableQuery query = RecoverableQuery.query(session.stub(),currentSaveIndex,PresencePortableRegistry.INS);
        List<CurrentSaveIndex> list = dataStore.list(query);
        if(list.size()==0) {
            currentSaveIndex.ownerKey(query.key());
            currentSaveIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            dataStore.create(currentSaveIndex);
            list.add(currentSaveIndex);
        }
        currentSaveIndex = list.get(0);
        if(currentSaveIndex.saveId == 0){
            SavedGame savedGame = savedGameList(session).get(0); //select first one always if no selection from players
            savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            savedGame.stub = session.stub();
            dataStore.update(savedGame);
            currentSaveIndex.saveId = savedGame.distributionId();
            currentSaveIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            dataStore.update(currentSaveIndex);
        }
        return currentSaveIndex;
    }

    public CurrentSaveIndex selectSavedGame(Session session){
        long selected = Long.parseLong(session.name());
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        if(selected==currentSaveIndex.saveId) return currentSaveIndex;
        SavedGame savedGame = new SavedGame();
        savedGame.distributionId(selected);
        if(!dataStore.load(savedGame)) return currentSaveIndex;
        SavedGame released = new SavedGame();
        released.distributionId(currentSaveIndex.saveId);
        if(!dataStore.load(released)) throw new RuntimeException("save not existed ["+currentSaveIndex.saveId+"]");
        currentSaveIndex.saveId = selected;
        currentSaveIndex.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        dataStore.update(currentSaveIndex);
        released.stub = 0;
        dataStore.update(released);
        savedGame.stub = session.stub();
        dataStore.update(savedGame);
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
        RecoverableQuery<SavedGame> query = new RecoverableQuery<>(session.key(),SavedGame.USER_SAVE,PresencePortableRegistry.SAVED_GAME_CID,PresencePortableRegistry.INS);
        List<SavedGame> list = dataStore.list(query);
        if(list.size()==0){
            for(int i=0;i<saveSize;i++){
                SavedGame save = new SavedGame();
                save.name("save"+i);
                save.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                save.version = 1;
                save.label(SavedGame.USER_SAVE);
                save.ownerKey(session.key());
                dataStore.create(save);
                list.add(save);
            }
        }
        return list;
    }

}
