package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class CurrentSaveIndex extends RecoverableObject {

    //session  <> save Id mapping for current save selection or default save

    public static final String LABEL = "currentSaveIndex";

    public long saveId;

    public CurrentSaveIndex(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public boolean read(DataBuffer buffer){
        this.saveId = buffer.readLong();
        this.timestamp = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(saveId);
        buffer.writeLong(timestamp);
        return true;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.CURRENT_SAVE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("GameId",Long.toString(this.saveId));
        jsonObject.addProperty("Timestamp",Long.toString(timestamp));
        return jsonObject;
    }

}
