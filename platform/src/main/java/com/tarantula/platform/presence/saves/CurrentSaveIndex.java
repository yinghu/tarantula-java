package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class CurrentSaveIndex extends RecoverableObject {


    public static final String LABEL = "currentSaveIndex";

    public long saveId;
    public long stub;

    public CurrentSaveIndex(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public boolean read(DataBuffer buffer){
        this.saveId = buffer.readLong();
        this.stub = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(saveId);
        buffer.writeLong(stub);
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

    public static CurrentSaveIndex lookup(Session session, DataStore dataStore){
        CurrentSaveIndex[] currentSaveIndices = new CurrentSaveIndex[]{null};
        dataStore.list(new CurrentSaveIndexQuery(session),current->{
            if(current.stub==session.stub()){
                currentSaveIndices[0]=current;
                currentSaveIndices[0].dataStore(dataStore);
                return false;
            }
            return true;
        });
        if(currentSaveIndices[0]!=null) return currentSaveIndices[0];
        currentSaveIndices[0] = new CurrentSaveIndex();
        currentSaveIndices[0].stub = session.stub();
        currentSaveIndices[0].ownerKey(session.key());
        dataStore.create(currentSaveIndices[0]);
        currentSaveIndices[0].dataStore(dataStore);
        return currentSaveIndices[0];
    }

}
