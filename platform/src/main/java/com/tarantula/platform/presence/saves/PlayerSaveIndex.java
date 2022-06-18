package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerSaveIndex extends IndexSet {

    public PlayerSaveIndex(){
        this.label = "playerSaveIndex";
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.PLAYER_SAVE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray keys = new JsonArray();
        keySet().forEach(k->keys.add(k));
        jsonObject.add("_keys",keys);
        return jsonObject;
    }
}
