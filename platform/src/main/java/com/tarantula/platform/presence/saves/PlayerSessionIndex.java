package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerSessionIndex extends IndexSet {

    //Save id or system ID if no save selected = > Data ID

    public PlayerSessionIndex(){
        this.label = "playerSessionIndex";
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
        keySet().forEach(k->keys.add(k));
        jsonObject.add("_keys",keys);
        return jsonObject;
    }
}
