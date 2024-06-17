package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;

import com.icodesoftware.Session;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;

import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.platform.item.PlatformItemServiceProvider;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;



public class PlatformSavedGameServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "save";

    private final int mappingObjectMaxSize = EnvSetting.VALUE_SIZE;
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
        saveSize = saveGame.get("saveSize").getAsInt();
        saveTimeout = saveGame.get("saveTimeout").getAsInt()*saveTimeout;
        dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformSavedGameServiceProvider.class);
        this.logger.warn("Saved game service provider started on ->"+gameServiceName+" : Max mapping object size : "+mappingObjectMaxSize);
    }


    public <T extends RecoverableObject> void save(Session session,T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        PlayerSaveIndex saveIndex = playerSaveIndex(currentSaveIndex.saveId==0?session.systemId():currentSaveIndex.saveId);
        save.distributionKey(saveIndex.distributionKey());
        if(!this.dataStore.update(save)) {
            this.dataStore.createIfAbsent(save, false);
        }
        save.dataStore(dataStore);
        //if(saveIndex.addKey(save.key().asString())) saveIndex.update();
        //.presenceServiceProvider().updateSavedGame(currentSaveIndex);
    }



    public <T extends RecoverableObject> boolean load(Session session, T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        long saveId = currentSaveIndex.saveId==0?session.systemId():currentSaveIndex.saveId;
        save.distributionId(saveId);
        save.dataStore(dataStore);
        return this.dataStore.load(save);
    }

    public boolean reset(Session session){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        SavedGame savedGame = SavedGame.lookup(currentSaveIndex.saveId,dataStore);
        if(savedGame==null || savedGame.stub != currentSaveIndex.stub) return false;
        //platformGameServiceProvider.presenceServiceProvider().onResetSavedGame(savedGame);
        savedGame.version = 1;
        savedGame.name("New Save");
        savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        savedGame.update();
        return true;
    }

    public CurrentSaveIndex currentSaveIndex(Session session){
        CurrentSaveIndex currentSaveIndex = CurrentSaveIndex.lookup(session,dataStore);
        if(currentSaveIndex.saveId == 0){
            SavedGame savedGame = savedGameList(session).get(0); //select first one always if no selection from players
            savedGame.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            savedGame.stub = session.stub();
            dataStore.update(savedGame);
            currentSaveIndex.saveId = savedGame.distributionId();
            currentSaveIndex.update();
        }
        return currentSaveIndex;
    }

    public CurrentSaveIndex selectSavedGame(Session session){
        long selected = Long.parseLong(session.name());
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        if(selected == currentSaveIndex.saveId) return currentSaveIndex;

        SavedGame savedGame = SavedGame.lookup(selected,dataStore);
        if(savedGame==null) return currentSaveIndex;

        SavedGame released = SavedGame.lookup(currentSaveIndex.saveId,dataStore);
        if(released==null) throw new RuntimeException("save not existed ["+currentSaveIndex.saveId+"]");

        currentSaveIndex.saveId = selected;
        currentSaveIndex.update();

        released.stub = 0;
        released.update();

        savedGame.stub = session.stub();
        savedGame.update();
        return currentSaveIndex;
    }

    private PlayerSaveIndex playerSaveIndex(long indexId){
        PlayerSaveIndex playerSaveIndex = new PlayerSaveIndex(indexId);
        this.dataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(this.dataStore);
        return playerSaveIndex;
    }


    public List<SavedGame> savedGameList(Session session){
        return SavedGame.list(session,dataStore,saveSize);
    }

    public void saveData(Session session,String key,byte[] data){
        OversizeDataIndex[] indexed = {null};
        dataStore.list(new OversizeDataIndexQuery(SnowflakeKey.from(session.distributionId())),(index)->{
            if(!index.saveKey.equals(key)) return true;
            indexed[0] = index;
            return false;
        });
        HashMap<Integer,byte[]> chunks = OversizeDataBatch.toBatch(data,mappingObjectMaxSize-BatchedMappingObject.MAP_OBJECT_HEADER_SIZE);
        if(indexed[0]==null){
            indexed[0] = new OversizeDataIndex(key);
            indexed[0].saveKey = key;
            indexed[0].batch = chunks.size();
            indexed[0].ownerKey(SnowflakeKey.from(session.distributionId()));
            dataStore.create(indexed[0]);
            for(int i=0;i<chunks.size();i++){
                byte[] chunk = chunks.get(i);
                BatchedMappingObject batchedMappingObject = new BatchedMappingObject(key);
                batchedMappingObject.value(chunk);
                batchedMappingObject.batch = i;
                batchedMappingObject.ownerKey(SnowflakeKey.from(indexed[0].distributionId()));
                dataStore.create(batchedMappingObject);
            }
            return;
        }
        indexed[0].batch = chunks.size();
        dataStore.update(indexed[0]);
        dataStore.list(new OversizeDataQuery(SnowflakeKey.from(indexed[0].distributionId()),key),(chunk)->{
            byte[] pending = chunks.remove(chunk.batch);
            if(pending!=null){
                chunk.value(pending);
                dataStore.update(chunk);
            }
            return true;
        });
        chunks.forEach((k,v)->{
            BatchedMappingObject batchedMappingObject = new BatchedMappingObject(key);
            batchedMappingObject.value(v);
            batchedMappingObject.batch = k;
            batchedMappingObject.ownerKey(SnowflakeKey.from(indexed[0].distributionId()));
            dataStore.create(batchedMappingObject);
        });
    }

    public byte[] loadData(Session session, String key){
        OversizeDataIndex[] indexed = {null};
        dataStore.list(new OversizeDataIndexQuery(SnowflakeKey.from(session.distributionId())),(index)->{
            if(!index.saveKey.equals(key)) return true;
            indexed[0] = index;
            return false;
        });
        if(indexed[0]==null) return null;
        HashMap<Integer,BatchedMappingObject> batchData = new HashMap<>();
        dataStore.list(new OversizeDataQuery(SnowflakeKey.from(indexed[0].distributionId()),key),(batch)->{
            if(batch.batch<indexed[0].batch) batchData.put(batch.batch,batch);
            return true;
        });
        return OversizeDataBatch.fromBatch(batchData);
    }



}
