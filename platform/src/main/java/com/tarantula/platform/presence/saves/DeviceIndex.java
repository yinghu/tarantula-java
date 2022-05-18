package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;

//device id => systemId index set
public class DeviceIndex extends IndexSet {

    public DeviceIndex(){
        this.label = "deviceIndex";
    }
    public DeviceIndex(String deviceId){
        this();
        this.index = deviceId;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DEVICE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    public Key key(){
        return new NaturalKey(this.index);
    }
    public List<SavedGame> list(){
        List<SavedGame> savedGames = new ArrayList<>();
        keySet.forEach(k->{
            SavedGameIndex index = new SavedGameIndex();
            index.distributionKey(k);
            index.dataStore(dataStore);
            this.dataStore.load(index);
            savedGames.addAll(index.list());
        });
        return savedGames;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray keys = new JsonArray();
        keySet.forEach(k->keys.add(k));
        jsonObject.add("index",keys);
        return jsonObject;
    }
}