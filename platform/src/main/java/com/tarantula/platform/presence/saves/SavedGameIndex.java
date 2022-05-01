package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
        properties.forEach((k,v)->{
            SavedGame savedGame = new SavedGame();
            savedGame.distributionKey(k);
            if(this.dataStore.load(savedGame)){
                savedGames.add(savedGame);
            }
        });
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
        if(savedGames.add(savedGame)) return false;
        addKey(savedGame.distributionKey());
        dataStore.update(this);
        return true;
    }
    public List<SavedGame> list(String deviceId){
        ArrayList<SavedGame> _tem = new ArrayList<>();
        int[] created = {0};
        savedGames.forEach(save->{
            if(save.index().equals(deviceId)) created[0]++;
            _tem.add(save);
        });
        if(created[0]==0){//
            SavedGame savedGame = new SavedGame(deviceId);
            this.dataStore.create(savedGame);
            addSavedGame(savedGame);
            _tem.add(savedGame);
            DeviceIndex deviceIndex = new DeviceIndex(deviceId);
            this.dataStore.createIfAbsent(deviceIndex,true);
            deviceIndex.addKey(this.distributionKey());
            this.dataStore.update(deviceIndex);
        }
        return _tem;
    }


}