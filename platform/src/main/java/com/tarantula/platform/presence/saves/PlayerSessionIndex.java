package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.SimpleProperty;
import com.tarantula.platform.PropertyIndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;


public class PlayerSessionIndex extends PropertyIndexSet {

    //SystemId ==> activeSave

    public PlayerSessionIndex(){
        super("playerSessionIndex");

    }

    public PlayerSessionIndex(String systemId){
        this();
        this.distributionKey(systemId);
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.PLAYER_SESSION_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray keys = new JsonArray();
        keySet().forEach(k->keys.add(k.toJson()));
        jsonObject.add("_keys",keys);
        return jsonObject;
    }
    public boolean load(CurrentSaveIndex pending){
        Object value = value(pending.key().asString());
        if(value==null) return false;
        pending.parse((String) value);
        return true;
    }
    public void update(CurrentSaveIndex pending){
        SimpleProperty property = new SimpleProperty(pending.key().asString(),pending);
        removeKey(property);
        addKey(property);
        this.update();
    }
    private void delete(CurrentSaveIndex pending){
        SimpleProperty property = new SimpleProperty(pending.key().asString(),pending);
        removeKey(property);
        this.update();
    }
    public void check(long timeout,SavedGameSelected deleted){
        ArrayList<CurrentSaveIndex> expired = new ArrayList<>();
        keySet().forEach(k->{
            CurrentSaveIndex currentSaveIndex = new CurrentSaveIndex(k.name());
            if(load(currentSaveIndex) && currentSaveIndex.expired(timeout)){
                deleted.selected(currentSaveIndex);
                expired.add(currentSaveIndex);
            }
        });
        expired.forEach(c->delete(c));
    }
}
