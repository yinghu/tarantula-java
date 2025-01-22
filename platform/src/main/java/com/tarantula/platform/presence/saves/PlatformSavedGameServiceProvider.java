package com.tarantula.platform.presence.saves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;

import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.Transaction;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;

import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.service.deployment.ContentMapping;
import com.tarantula.platform.util.RecoverableQuery;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;


public class PlatformSavedGameServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "save";

    private int mappingObjectMaxSize = 2000;
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
        mappingObjectMaxSize = saveGame.get("mappingObjectMaxSize").getAsInt();
        maxOverSizeBatch = saveGame.get("oversizePayloadMaxBatchSize").getAsInt();
        oversizePayloadReadRetryNumber = saveGame.get("oversizePayloadReadRetryNumber").getAsInt();
        saveSize = saveGame.get("saveSize").getAsInt();
        saveTimeout = saveGame.get("saveTimeout").getAsInt()*saveTimeout;
        dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformSavedGameServiceProvider.class);
        this.logger.info("Saved game service provider started on ->"+gameServiceName+" : "+mappingObjectMaxSize);
    }


    public <T extends RecoverableObject> void save(Session session,T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        PlayerSaveIndex saveIndex = playerSaveIndex(currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index());
        save.distributionKey(saveIndex.distributionKey());
        if(!this.dataStore.update(save)) {
            this.dataStore.createIfAbsent(save, false);
        }
        save.dataStore(dataStore);
        //if(saveIndex.addKey(save.key().asString())) saveIndex.update();
        platformGameServiceProvider.presenceServiceProvider().updateSavedGame(currentSaveIndex);
    }

    public <T extends RecoverableObject> void createIfAbsent(Session session, T save){
        CurrentSaveIndex currentSaveIndex = currentSaveIndex(session);
        String saveId = currentSaveIndex.index()==null?session.systemId():currentSaveIndex.index();
        save.distributionKey(saveId);
        save.dataStore(dataStore);
        if(!this.dataStore.createIfAbsent(save,true)) return;
        PlayerSaveIndex saveIndex = playerSaveIndex(saveId);
        //if(saveIndex.addKey(save.key().asString())) saveIndex.update();
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
        //saveIndex.keySet().forEach(k->{
            //this.dataStore.delete(k);
        //});
        //saveIndex.clear();
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
    }
    private PlayerSaveIndex playerSaveIndex(String indexId){
        PlayerSaveIndex playerSaveIndex = new PlayerSaveIndex(indexId);
        this.dataStore.createIfAbsent(playerSaveIndex,true);
        playerSaveIndex.dataStore(this.dataStore);
        return playerSaveIndex;
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

    public void saveData(Session session,String key,byte[] data){
        HashMap<Integer,byte[]> chunks = OversizeDataBatch.toBatch(data,mappingObjectMaxSize-BatchedMappingObject.MAP_OBJECT_HEADER_SIZE);
        int chunkSize = chunks.size();
        if(chunkSize > maxOverSizeBatch){
            //throw new RuntimeException("oversize payload reached max chunk size ["+chunks.size()+"]");
            logger.info("Chunk size : "+chunkSize +" is over max batch size : "+maxOverSizeBatch);
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

    public SaveRevisionInfo saveRevisionInfo(Session session){
        SaveRevisionInfo saveRevisionInfo = new SaveRevisionInfo();
        saveRevisionInfo.distributionId(session.distributionId());
        saveRevisionInfo.name(session.name());
        dataStore.createIfAbsent(saveRevisionInfo,true);
        return saveRevisionInfo;
    }

    public boolean saveRevisionInfo(Session session,SaveRevisionInfo pending){
        SaveRevisionInfo saveRevisionInfo = new SaveRevisionInfo();
        saveRevisionInfo.distributionId(session.distributionId());
        saveRevisionInfo.name(pending.name());
        dataStore.createIfAbsent(saveRevisionInfo,true);
        if(pending.clientRevisionNumber != saveRevisionInfo.clientRevisionNumber+1) return false;
        saveRevisionInfo.clientRevisionNumber++;
        saveRevisionInfo.deviceId = pending.deviceId;
        return dataStore.update(saveRevisionInfo);
    }


}
