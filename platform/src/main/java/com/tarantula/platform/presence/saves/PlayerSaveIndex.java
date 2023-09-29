package com.tarantula.platform.presence.saves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerSaveIndex extends RecoverableObject {

    //Save id or system ID if no save selected = > Data ID

    public PlayerSaveIndex(){
        this.label = "playerSaveIndex";
    }

    public PlayerSaveIndex(String indexId){
        this();
        this.distributionKey(indexId);
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
        //keySet().forEach(k->keys.add(k));
        jsonObject.add("_keys",keys);
        return jsonObject;
    }
}
