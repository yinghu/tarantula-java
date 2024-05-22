package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;


public class ProfileNameSequence extends RecoverableObject {

    public static final String LABEL = "profile_name_sequence";

    private int sequence;

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

    public synchronized int sequence(){
        this.sequence++;
        this.update(); //have to sync update
        return sequence;
    }


}
