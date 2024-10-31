package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class PlatformBannedPlayer extends RecoverableObject {
    public static final String LABEL = "ban";

    public long systemId;

    public PlatformBannedPlayer(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public PlatformBannedPlayer(long systemId){
        this();
        this.systemId = systemId;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.BANNED_PLAYER_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    @Override
    public boolean write(Recoverable.DataBuffer buffer) {
        buffer.writeLong(systemId);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(Recoverable.DataBuffer buffer) {
        systemId = buffer.readLong();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("PlayerID",Long.toString(systemId));
        return jsonObject;
    }
}
