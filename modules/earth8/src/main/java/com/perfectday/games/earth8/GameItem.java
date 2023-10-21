package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class GameItem extends RecoverableObject {

    public String configId;
    public int level;
    public int xp;
    public int rank;

    @Override
    public boolean read(DataBuffer buffer) {
        configId = buffer.readUTF8();
        level = buffer.readInt();
        xp = buffer.readInt();
        rank = buffer.readInt();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(configId);
        buffer.writeInt(level);
        buffer.writeInt(xp);
        buffer.writeInt(rank);
        return true;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ItemId",distributionKey());
        jsonObject.addProperty("ConfigId",configId);
        jsonObject.addProperty("Level",level);
        jsonObject.addProperty("Xp",xp);
        jsonObject.addProperty("rank",rank);
        return jsonObject;
    }

}
