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
    public List<SavedGame> list(int saveSize){
        ArrayList<SavedGame> _tem = new ArrayList<>();
        int[] create = {0};
        savedGames.forEach(save-> {
            save.load();
            _tem.add(save);
            create[0]++;
        });
        while (create[0]<saveSize){//always create one if no save associated with deviceId
            SavedGame savedGame = new SavedGame(this.distributionKey(),"New Save ["+create[0]+"]");
            dataStore.create(savedGame);
            addSavedGame(savedGame);
            _tem.add(savedGame);
            create[0]++;
        }
        return _tem;
    }

    public SavedGame select(String saveId){
        SavedGame[] selected = {null};
        savedGames.forEach(save-> {
            save.load();
            if (save.distributionKey().equals(saveId)){
                selected[0]=save;
            }
        });
        return selected[0];
    }
}