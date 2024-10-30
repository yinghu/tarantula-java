package com.tarantula.platform.lobby;

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
        jsonObject.addProperty("PlayerID",systemId);
        return jsonObject;
    }
}
