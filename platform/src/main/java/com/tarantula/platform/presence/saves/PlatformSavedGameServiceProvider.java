package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.Session;

import com.icodesoftware.lmdb.EnvSetting;

import com.icodesoftware.Transaction;

import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
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
    private int maxOverSizeBatch = 12;
    private int oversizePayloadReadRetryNumber = 2;
    public PlatformSavedGameServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject saveGame = ((JsonElement)configuration.property("savedGame")).getAsJsonObject();

        //mappingObjectMaxSize = saveGame.get("mappingObjectMaxSize").getAsInt();
        maxOverSizeBatch = saveGame.get("oversizePayloadMaxBatchSize").getAsInt();
        oversizePayloadReadRetryNumber = saveGame.get("oversizePayloadReadRetryNumber").getAsInt();

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
        HashMap<Integer,byte[]> chunks = OversizeDataBatch.toBatch(data,mappingObjectMaxSize-BatchedMappingObject.MAP_OBJECT_HEADER_SIZE);
        int chunkSize = chunks.size();
        if(chunkSize > maxOverSizeBatch){
            //throw new RuntimeException("oversize payload reached max chunk size ["+chunks.size()+"]");
            logger.warn("Chunk size : "+chunkSize +" is over max batch size : "+maxOverSizeBatch);
        }
        Transaction transaction = gameCluster.transaction();
        try{
            transaction.execute(ctx->{
                ApplicationPreSetup preSetup = (ApplicationPreSetup)ctx;
                DataStore saveDataStore = preSetup.onDataStore(NAME);
                OversizeDataIndex index = OversizeDataIndex.createIfNotExisted(saveDataStore,session,chunkSize);
                saveDataStore.list(new OversizeDataQuery(SnowflakeKey.from(index.distributionId()),key)).forEach((chunk)->{
                    byte[] pending = chunks.remove(chunk.batch);
                    if(pending!=null){
                        chunk.value(pending);
                        saveDataStore.update(chunk);
                    }
                });
                chunks.forEach((k,v)->{
                    BatchedMappingObject batchedMappingObject = new BatchedMappingObject(key);
                    batchedMappingObject.value(v);
                    batchedMappingObject.batch = k;
                    batchedMappingObject.ownerKey(SnowflakeKey.from(index.distributionId()));
                    saveDataStore.create(batchedMappingObject);
                });
                return true;
            });
        }finally {
            transaction.close();
        }
    }

    public byte[] loadData(Session session, String key){
        OversizeDataIndex indexed = OversizeDataIndex.load(dataStore,session);
        if(indexed==null){
            logger.warn("Over-sized data index not ready for load ["+key+"]");
            return null;
        }
        HashMap<Integer,BatchedMappingObject> batchData = new HashMap<>();
        boolean loaded = false;
        for(int i=0; i<oversizePayloadReadRetryNumber; i++){
            final OversizeDataIndex index = indexed;
            dataStore.list(new OversizeDataQuery(SnowflakeKey.from(indexed.distributionId()),key),(batch)->{
                if(batch.batch<index.batch) batchData.put(batch.batch,batch);
                return true;
            });
            OversizeDataIndex verified = OversizeDataIndex.load(dataStore,session);
            if(verified.revision()==indexed.revision()){
                loaded = true;
                break;
            }
            logger.warn("Oversize payload retrying ["+i+"]");
            indexed = verified;
        }
        return loaded? OversizeDataBatch.fromBatch(batchData):null;
    }



}
