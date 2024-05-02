package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

public class ProfileNameSequence extends RecoverableObject {

    public static final String LABEL = "profile_name_sequence";

    public int sequence;

    public ProfileNameSequence(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public ProfileNameSequence(String name){
        this();
        this.name = name;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.PROFILE_NAME_SEQUENCE_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        sequence = buffer.readInt();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeInt(sequence);
        return true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Name",name);
        resp.addProperty("Sequence",sequence);
        return resp;
    }

    public static ProfileNameSequence lookup(DataStore dataStore,long gameClusterId, String pendingMame){
        ProfileNameSequence[] pending = {null};
        dataStore.list(new ProfileNameSequenceQuery(gameClusterId),profileNameSequence -> {
            if(profileNameSequence.name.equals(pendingMame)){
                pending[0]=profileNameSequence;
                return false;
            }
            return true;
        });
        if(pending[0]!=null) return pending[0];
        pending[0] = new ProfileNameSequence(pendingMame);
        pending[0].ownerKey(SnowflakeKey.from(gameClusterId));
        dataStore.create(pending[0]);
        pending[0].dataStore(dataStore);
        return pending[0];
    }
}
