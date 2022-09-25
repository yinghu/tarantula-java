package com.tarantula.platform.presence.saves;

import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.*;

//system id => saved game id index set
public class SavedGameIndex extends IndexSet {

    private Set<SavedGame> savedGames  = new HashSet<>();

    public SavedGameIndex(){
        this.label = "savedGameIndex";
    }
    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
        keySet.forEach((k)->{
            SavedGame savedGame = new SavedGame();
            savedGame.distributionKey(k);
            savedGame.dataStore(this.dataStore);
            savedGames.add(savedGame);
        });
        properties.clear();
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.SAVE_GAME_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    public boolean addSavedGame(SavedGame savedGame){
        if(!savedGames.add(savedGame)) return false;
        addKey(savedGame.distributionKey());
        dataStore.update(this);
        return true;
    }
    public List<SavedGame> list(){
        ArrayList<SavedGame> _tem = new ArrayList<>();
        savedGames.forEach(save-> {
            save.load();
            _tem.add(save);
        });
        return _tem;
    }
    public List<SavedGame> list(String deviceId,String deviceName){
        ArrayList<SavedGame> _tem = new ArrayList<>();
        int[] created = {0};
        savedGames.forEach(save->{
            save.load();
            if(save.index().equals(deviceId) && save.owner().equals(this.distributionKey())) created[0]++;
            _tem.add(save);
        });
        if(created[0]==0){//
            SavedGame savedGame = new SavedGame(this.distributionKey(),deviceId,deviceName);
            savedGame.dataStore(dataStore);
            this.dataStore.create(savedGame);
            addSavedGame(savedGame);
            _tem.add(savedGame);
        }
        DeviceIndex deviceIndex = new DeviceIndex(deviceId);
        this.dataStore.createIfAbsent(deviceIndex,true);
        deviceIndex.dataStore(this.dataStore);
        deviceIndex.list().forEach(save->{
            if(addSavedGame(save)) _tem.add(save);
        });
        deviceIndex.addKey(this.distributionKey());
        this.dataStore.update(deviceIndex);
        return _tem;
    }


}