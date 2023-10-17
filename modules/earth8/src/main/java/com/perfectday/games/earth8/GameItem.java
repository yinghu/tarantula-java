package com.perfectday.games.earth8;

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
}
