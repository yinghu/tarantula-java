package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class BannedPlayer extends RecoverableObject {

    public static final String LABEL = "ban";

    public long systemId;

    public BannedPlayer(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public BannedPlayer(long systemId){
        this();
        this.systemId = systemId;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(systemId);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        systemId = buffer.readLong();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SystemId",systemId);
        return jsonObject;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }


}
